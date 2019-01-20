package com.ilyshav.gallery

import cats.effect.Sync

class GalleryService[F[_]](config: Config)(implicit F: Sync[F]) {

  def start(): F[Unit] = {
    ???
  }
}
