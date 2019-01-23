package com.ilyshav.gallery.process

import com.ilyshav.gallery.PrivateModels.{Album, Photo}
import fs2.{Stream => Stream}

object SearchPhotos {
  def run[F[_]](album: Album): Stream[F, Photo] = {
    ???
  }
}
