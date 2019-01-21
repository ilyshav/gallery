package com.ilyshav.gallery.process

import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit

import cats.effect.{Bracket, Sync, Timer}
import com.ilyshav.gallery.Database
import org.slf4j.LoggerFactory

object ScanAlbums {
  import scala.collection.JavaConverters._
  import cats.syntax.flatMap._
  import cats.syntax.functor._

  private val logger = LoggerFactory.getLogger(this.getClass)

  def fullScan[F[_]](albumsPath: Path, db: Database[F])(
      implicit F: Sync[F],
      B: Bracket[F, Throwable],
      T: Timer[F]): F[Unit] =
    for {
      _ <- F.delay(logger.debug("Open file system walker"))
      now <- T.clock.realTime(TimeUnit.SECONDS)
      normalizedPath = albumsPath.normalize()
      r <- B.bracket(F.delay(Files.walk(albumsPath))) { walkerStream =>
        fs2.Stream
          .fromIterator(walkerStream.filter(p => Files.isDirectory(p)).iterator().asScala)
          .covary[F]
          .map(p => normalizedPath.relativize(p).normalize().toString)
          .filter(!_.isBlank)
          .evalMap(p => db.saveAlbum(p, now))
          .compile
          .drain
      }(walkerStream => F.delay {
        logger.debug("Closing file system walker")
        walkerStream.close()
      })
    } yield r

}
