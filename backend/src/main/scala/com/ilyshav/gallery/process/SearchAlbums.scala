package com.ilyshav.gallery.process

import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit

import cats.effect.{Bracket, Sync, Timer}
import com.ilyshav.gallery.Database
import com.ilyshav.gallery.PrivateModels.Album
import org.slf4j.LoggerFactory
import fs2.Stream

object SearchAlbums {
  import scala.collection.JavaConverters._
  private val logger = LoggerFactory.getLogger(this.getClass)

  def fullScan[F[_]](albumsPath: Path, db: Database[F])(
      implicit F: Sync[F],
      B: Bracket[F, Throwable],
      T: Timer[F]): fs2.Stream[F, Album] =
    for {
      _ <- Stream.eval(F.delay(logger.debug("Open file system walker")))
      now <- Stream.eval(T.clock.realTime(TimeUnit.SECONDS))
      normalizedPath = albumsPath.normalize()
      iterator <- Stream
        .bracket(F.delay(Files.walk(albumsPath)))(w => F.delay(w.close()))
        .map { walker =>
          walker.filter(p => Files.isDirectory(p)).iterator().asScala
        }
        .covary[F]
      r <- Stream
        .fromIterator(iterator)
        .map(p => normalizedPath.relativize(p).normalize().toString)
        .filter(!_.isBlank)
        .evalMap(p => db.saveAlbum(p, now))
    } yield r

}
