package com.ilyshav.gallery.process

import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit

import cats.effect.{Bracket, Sync, Timer}
import com.ilyshav.gallery.Database

object ScanAlbums {
  import scala.collection.JavaConverters._
  import cats.syntax.flatMap._
  import cats.syntax.functor._

  // todo close stream
  def fullScan[F[_]](albumsPath: Path, db: Database[F])(
      implicit F: Sync[F],
      B: Bracket[F, Throwable],
      T: Timer[F]): F[Unit] =
    for {
      now <- T.clock.realTime(TimeUnit.SECONDS)
      r <- B.bracket(F.delay(Files.walk(albumsPath))) { walkerStream =>
        fs2.Stream
          .fromIterator(walkerStream.filter(p => Files.isDirectory(p)).iterator().asScala)
          .covary[F] // todo filter out "." path
          .map(p => p.relativize(albumsPath).normalize().toString)
          .evalMap(p => db.saveAlbum(p, now))
          .compile
          .drain
      }(walkerStream => F.delay {
        println("closed")
        walkerStream.close()
      })
    } yield r

}
