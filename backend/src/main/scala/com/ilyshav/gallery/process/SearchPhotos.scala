package com.ilyshav.gallery.process

import java.nio.file.{Files, Path}

import cats.effect.Sync
import com.ilyshav.gallery.Database
import com.ilyshav.gallery.PrivateModels.{Album, Photo}
import fs2.Stream

object SearchPhotos {

  val supportedFiles: Set[String] = Set("jpg", "png", "gif")

  def run[F[_]](album: Album, db: Database[F], root: Path)(implicit F: Sync[F]): Stream[F, Photo] = {
    import scala.collection.JavaConverters._

    for {
      iter <- Stream.bracket(F.delay(Files.walk(album.fullPath(root))))(w => F.delay(w.close()))
        .map(_.filter(f => Files.isRegularFile(f) && isSupported(f.getFileName.toString)))
      stream <- Stream.fromIterator(iter.iterator().asScala)
          .evalMap { f =>
            val p = album.fullPath(root).resolve(f)
            db.savePhoto(p.toString, album.id)
          }
    } yield stream
  }

  private def isSupported(filename: String): Boolean = {
    supportedFiles.contains(filename.split('.').last)
  }
}
