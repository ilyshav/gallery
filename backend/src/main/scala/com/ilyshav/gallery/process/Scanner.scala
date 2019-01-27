package com.ilyshav.gallery.process

import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit

import cats.Functor
import cats.effect.{Bracket, Sync, Timer}
import com.ilyshav.gallery.Database
import com.ilyshav.gallery.PrivateModels.Album
import org.slf4j.LoggerFactory
import fs2.Stream

object Scanner {
  import scala.collection.JavaConverters._
  private val logger = LoggerFactory.getLogger(this.getClass)

  val supportedFiles: Set[String] = Set("jpg", "png", "gif")

  def stream[F[_]](galleryRoot: Path, db: Database[F])(
      implicit F: Sync[F],
      B: Bracket[F, Throwable],
      T: Timer[F]): fs2.Stream[F, Unit] = {
    streamAux(galleryRoot, db, Album.root)
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

  def processFile[F[_]: Functor](path: Path,
                                 album: Album,
                                 db: Database[F]): F[Unit] = {
    import cats.syntax.functor._

    db.savePhoto(path.toString, album.id).map(_ => ())
  }

  private def isSupported(filename: String): Boolean = {
    supportedFiles.contains(filename.split('.').last)
  }
}
