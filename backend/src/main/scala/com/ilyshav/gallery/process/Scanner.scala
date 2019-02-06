package com.ilyshav.gallery.process

import java.io.File
import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit

import cats.Functor
import cats.effect.{Concurrent, ContextShift, Sync, Timer}
import com.ilyshav.gallery.{Config, Database}
import com.ilyshav.gallery.PrivateModels.{Album, Photo}
import org.slf4j.LoggerFactory
import fs2.Stream
import javax.imageio.ImageIO
import net.coobird.thumbnailator.Thumbnails

import scala.concurrent.ExecutionContext

object Scanner {
  import cats.syntax.flatMap._
  import cats.syntax.functor._

  import scala.collection.JavaConverters._
  private val logger = LoggerFactory.getLogger(this.getClass)

  val supportedFiles: Set[String] = Set("jpg", "png", "gif")

  def stream[F[_]: Concurrent: ContextShift](
      config: Config,
      db: Database[F],
      blocking: ExecutionContext)(implicit T: Timer[F]): fs2.Stream[F, Unit] =
    Stream.eval(db.getLastFullScan()).flatMap {
      case Some(_) => Stream.emit(()).covary[F]
      case None =>
        Stream
          .eval(Sync[F].delay(logger.info("Need to perform full scan")))
          .flatMap { _ =>
            streamAux(config, blocking)(config.galleryRoot, db, Album.root) ++ Stream
              .eval {
                for {
                  now <- T.clock.realTime(TimeUnit.SECONDS)
                  _ <- db.setLastFullScan(now)
                } yield ()
              }
          } ++ Stream.eval(Sync[F].delay(logger.info("Full scan completed")))
    }

  private def streamAux[F[_]: ContextShift](
      config: Config,
      blocking: ExecutionContext)(start: Path, db: Database[F], parent: Album)(
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
                streamAux(config, blocking)(path, db, album)
              }
          } else {
            Stream.eval(processFile(config, blocking)(path, parent, db))
          }
        }
    } yield r
  }

  private def processFile[F[_]: Functor: Concurrent: ContextShift](
      config: Config,
      blocking: ExecutionContext)(path: Path,
                                  album: Album,
                                  db: Database[F]): F[Unit] = {
    if (isSupported(path.getFileName.toString)) {

      for {
        size <- getPhotoSize(path, blocking)
        (width, height) = size
        photo <- db.savePhoto(path.toString, album.id, width, height)
        _ <- createThumbnail(config, db, blocking)(photo)
      } yield ()
    } else Concurrent[F].unit
  }

  private def createThumbnail[F[_]: Concurrent](config: Config,
                                                db: Database[F],
                                                blocking: ExecutionContext)(
      photo: Photo)(implicit context: ContextShift[F]): F[Unit] =
    context.evalOn(blocking)(for {
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
    } yield ())

  private def getPhotoSize[F[_]: Sync](path: Path, blocking: ExecutionContext)(implicit context: ContextShift[F]): F[(Int, Int)] = {
    context.evalOn(blocking) {
      Sync[F].delay { // todo bracket
        val x = ImageIO.createImageInputStream(new File(path.toString))
        val reader = ImageIO.getImageReaders(x)
        reader.asScala.toList.headOption.map { reader =>
          reader.setInput(x)
          val width = reader.getWidth(0)
          val height = reader.getHeight(0)
          reader.dispose()
          (width, height)
        }.getOrElse(throw new RuntimeException("Unsupported image format"))
      }
    }
  }

  private def isSupported(filename: String): Boolean = {
    supportedFiles.contains(filename.split('.').last)
  }
}
