package com.ilyshav.gallery

import java.nio.file.Paths

import cats.effect.{Sync, Timer}
import com.ilyshav.gallery.process.ScanAlbums

class GalleryService[F[_]: Timer](config: Config, db: Database[F])(implicit F: Sync[F]) {
  import cats.syntax.flatMap._
  import cats.syntax.functor._

  def start(): F[Unit] = {
    fullAlbumsScan()
  }

  private def fullAlbumsScan(): F[Unit] = for {
    path <- F.delay(config.galleryDir)
    result <- ScanAlbums.fullScan(path, db)
  } yield result
}
