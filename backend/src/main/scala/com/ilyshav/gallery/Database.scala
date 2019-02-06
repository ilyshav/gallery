package com.ilyshav.gallery

import java.time.Instant
import java.util.UUID

import cats.effect.{Async, ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.ilyshav.gallery.HttpModels.{AlbumId, PhotoId, ThumbnailId}
import com.ilyshav.gallery.PrivateModels.{Album, Photo, Thumbnail}
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import doobie.implicits.toSqlInterpolator
import doobie.implicits.toConnectionIOOps

class Database[F[_]: Async: ContextShift](transactor: HikariTransactor[F])(
    implicit F: Sync[F]) {

  def saveAlbum(path: String,
                checkTimestamp: Long,
                parent: AlbumId): F[Album] = {
    val id = UUID.randomUUID().toString

    val selectSql = sql"select id from albums where path=$path"
    val insertSql =
      sql"""
           | insert into albums(id, path, lastCheck, name, parentAlbumId)
           | values ($id, $path, $checkTimestamp, $path, $parent) on conflict(path) do update set lastCheck=$checkTimestamp;
         """.stripMargin

    selectSql.query[String].option.transact(transactor).flatMap { existingId =>
      existingId.fold {
        insertSql.update.run
          .transact(transactor)
          .map(_ => Album(AlbumId(id), path, path, Some(parent)))
      }(id => F.delay(Album(AlbumId(id), path, path, Some(parent))))
    }
  }

  def getAlbums(parent: AlbumId = Album.root.id): F[List[Album]] = {
    val sql =
      sql"""
           |select id, path, name, parentAlbumId from albums a
           |  where a.parentAlbumId = ${parent.id}
         """.stripMargin

    sql.query[Album].to[List].transact(transactor)
  }

  def savePhoto(path: String,
                album: AlbumId,
                width: Int,
                height: Int): F[Photo] = {
    val id = UUID.randomUUID().toString

    val sql =
      sql"""
           | insert or ignore into photos(id, realPath, albumId, width, height)
           | values (${id}, ${path}, ${album.id}, ${width}, ${height})
         """.stripMargin

    sql.update.run
      .transact(transactor)
      .map(_ => Photo(PhotoId(id), path, thumbnail = None, width, height))
  }

  def getPhotos(album: AlbumId): F[List[Photo]] = {
    val sql =
      sql"""
           |select p.id, p.realPath, p.thumbnailId, width, height from photos p
           |  join albums a on p.albumId = a.id and a.id = ${album.id}
         """.stripMargin

    sql.query[Photo].to[List].transact(transactor)
  }

  def getPhoto(id: PhotoId): F[Option[Photo]] = {
    val sql =
      sql"select p.id, p.realPath, p.thumbnailId, p.width, p.height from photos p where p.id = ${id.id}"

    sql.query[Photo].option.transact(transactor)
  }

  def getLastFullScan(): F[Option[Instant]] = {
    val sql = sql"select lastFullScan from full_scan_metadata limit 1;"

    sql
      .query[Long]
      .option
      .transact(transactor)
      .map(_.map(Instant.ofEpochSecond))
  }

  def setLastFullScan(ts: Long): F[Unit] = {
    val sql = sql"insert into full_scan_metadata(lastFullScan) values (${ts});"

    sql.update.run.transact(transactor).map(_ => ())
  }

  def saveThumbnail(photoId: PhotoId, path: String): F[ThumbnailId] = {
    val id = UUID.randomUUID().toString

    val thumbnailSaveSql =
      sql"insert into thumbnails(id, realPath) values (${id}, ${path});"
    val setThumbnailForPhotoSql =
      sql"update photos set thumbnailId=${id} where id=${photoId.id};"

    (for {
      _ <- thumbnailSaveSql.update.run
      _ <- setThumbnailForPhotoSql.update.run
    } yield ThumbnailId(id)).transact(transactor)
  }

  def getThumbnail(thumbnailId: ThumbnailId): F[Option[Thumbnail]] = {
    val sql =
      sql"select id, realPath from thumbnails where id=${thumbnailId.id}"

    sql.query[Thumbnail].option.transact(transactor)
  }
}

object Database {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def open[F[_]: Async: ContextShift](
      config: Config,
      transactor: HikariTransactor[F]): F[Database[F]] =
    for {
      _ <- applyMigrations(config.dbPath)
    } yield new Database[F](transactor)

  private def applyMigrations[F[_]](dbPath: String)(
      implicit F: Sync[F]): F[Unit] =
    for {
      migrator <- F.delay {
        Flyway.configure
          .dataSource(s"jdbc:sqlite:file:$dbPath", null, null)
          .locations("classpath:db.migrations")
          .load()
      }
      appliedMigrations <- F.delay(migrator.migrate())
      _ <- F.delay(logger.info(s"Applied $appliedMigrations migrations."))
    } yield ()
}
