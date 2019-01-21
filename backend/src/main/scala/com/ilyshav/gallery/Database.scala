package com.ilyshav.gallery

import cats.effect.{Async, ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

import doobie.implicits.toSqlInterpolator
import doobie.implicits.toConnectionIOOps

class Database[F[_]: Async: ContextShift](config: Config, transactor: HikariTransactor[F])(
    implicit F: Sync[F]) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def saveAlbum(path: String, checkTimestamp: Long): F[Unit] = {
    val sql =
      sql"""
           | insert into albums(path, lastCheck)
           | values ($path, $checkTimestamp) on conflict(path) do update set lastCheck=$checkTimestamp;
         """.stripMargin

    for {
      _ <- F.delay(logger.debug(s"Saving album: $path. Checked at $checkTimestamp"))
      _ <- sql.update.run.transact(transactor)
    } yield ()
  }
}

object Database {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def open[F[_]: Async: ContextShift](config: Config, transactor: HikariTransactor[F]): F[Database[F]] =
    for {
      _ <- applyMigrations(config.dbPath)
    } yield new Database[F](config, transactor)

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
