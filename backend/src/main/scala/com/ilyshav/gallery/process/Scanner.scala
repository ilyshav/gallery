package com.ilyshav.gallery.process

import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit

import cats.Functor
import cats.effect.{Concurrent, Sync, Timer}
import com.ilyshav.gallery.{Config, Database}
import com.ilyshav.gallery.PrivateModels.{Album, Photo}
import org.slf4j.LoggerFactory
import fs2.Stream
import net.coobird.thumbnailator.Thumbnails

// todo blocking ec for all operations here
object Scanner {
  import cats.syntax.flatMap._
  import cats.syntax.functor._

  import scala.collection.JavaConverters._
  private val logger = LoggerFactory.getLogger(this.getClass)

  val supportedFiles: Set[String] = Set("jpg", "png", "gif")

  def stream[F[_]: Concurrent](config: Config, db: Database[F])(
      implicit T: Timer[F]): fs2.Stream[F, Unit] =
    Stream.eval(db.getLastFullScan()).flatMap {
      case Some(_) => Stream.emit(()).covary[F]
      case None =>
        Stream
          .eval(Sync[F].delay(logger.info("Need to perform full scan")))
          .flatMap { _ =>
            streamAux(config)(config.galleryRoot, db, Album.root) ++ Stream
              .eval {
                for {
                  now <- T.clock.realTime(TimeUnit.SECONDS)
                  _ <- db.setLastFullScan(now)
                } yield ()
              }
          } ++ Stream.eval(Sync[F].delay(logger.info("Full scan completed")))
    }

  private def streamAux[F[_]](
      config: Config)(start: Path, db: Database[F], parent: Album)(
      implicit F: Concurrent[F],
      T: Timer[F]): Stream[F, Unit] = {
    for {
      _ <- Stream.eval(F.delay(logger.debug("Open file system walker")))
      now <- Stream.eval(T.clock.realTime(TimeUnit.SECONDS))
      walker <- Stream.bracket(F.delay(Files.newDirectoryStream(start)))(w =>
        F.delay(w.close()))
      r <- Stream
        .fromIterator(walker.iterator().asScala)
        .flatMap { path =>
          if (Files.isDirectory(path)) {
            val normalizedPath = start.relativize(path).normalize().toString
            Stream
              .eval(db.saveAlbum(normalizedPath, now, parent.id))
              .flatMap { album =>
                streamAux(config)(path, db, album)
              }
          } else {
            Stream.eval(processFile(config)(path, parent, db))
          }
        }
    } yield r
  }

  private def processFile[F[_]: Functor: Concurrent](
      config: Config)(path: Path, album: Album, db: Database[F]): F[Unit] = {
    if (isSupported(path.getFileName.toString))
      db.savePhoto(path.toString, album.id).flatMap { photo =>
        createThumbnail(config, db)(photo)
      } else Concurrent[F].unit
  }

  private def createThumbnail[F[_]: Concurrent](
      config: Config,
      db: Database[F])(photo: Photo): F[Unit] =
    for {
      thumbnailPath <- Sync[F].delay(
        photo.thumbnailPath(config.galleryRoot, config.thumbnailsRoot))
      _ <- Sync[F].delay(thumbnailPath.getParentFile.mkdirs())
      _ <- Sync[F].delay {
        Thumbnails
          .of(photo.path)
          .size(300, 300)
          .outputFormat("jpeg")
          .toFile(thumbnailPath)
      }
      _ <- db.saveThumbnail(photo.id, thumbnailPath.getAbsolutePath)
    } yield ()

  private def isSupported(filename: String): Boolean = {
    supportedFiles.contains(filename.split('.').last)
  }
}
