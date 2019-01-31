package com.ilyshav.gallery.process

import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit

import cats.Functor
import cats.effect.{Sync, Timer}
import com.ilyshav.gallery.Database
import com.ilyshav.gallery.PrivateModels.Album
import org.slf4j.LoggerFactory
import fs2.Stream

object Scanner {
  import cats.syntax.flatMap._
  import cats.syntax.functor._

  import scala.collection.JavaConverters._
  private val logger = LoggerFactory.getLogger(this.getClass)

  val supportedFiles: Set[String] = Set("jpg", "png", "gif")

  def stream[F[_]: Sync](galleryRoot: Path, db: Database[F])(
      implicit T: Timer[F]): fs2.Stream[F, Unit] =
    Stream.eval(db.getLastFullScan()).flatMap {
      case Some(_) => Stream.emit(()).covary[F]
      case None =>
        Stream
          .eval(Sync[F].delay(logger.info("Need to perform full scan")))
          .flatMap { _ =>
            streamAux(galleryRoot, db, Album.root) ++ Stream.eval {
              for {
                now <- T.clock.realTime(TimeUnit.SECONDS)
                _ <- db.setLastFullScan(now)
              } yield ()
            }
          } ++ Stream.eval(Sync[F].delay(logger.info("Full scan completed")))
    }

  def streamAux[F[_]](start: Path, db: Database[F], parent: Album)(
      implicit F: Sync[F],
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
                streamAux(path, db, album)
              }
          } else {
            Stream.eval(processFile(path, parent, db))
          }
        }
    } yield r
  }

  def processFile[F[_]: Functor](path: Path, album: Album, db: Database[F])(
      implicit F: Sync[F]): F[Unit] = {
    import cats.syntax.functor._

    if (isSupported(path.getFileName.toString))
      db.savePhoto(path.toString, album.id).map(_ => ())
    else F.unit
  }

  private def isSupported(filename: String): Boolean = {
    supportedFiles.contains(filename.split('.').last)
  }
}
