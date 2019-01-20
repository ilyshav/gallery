package com.ilyshav.gallery

import cats.effect.Sync

class Config private() {
  import Config._

  val galleryDir: String = sys.env(EnvVar.galleryDir)
  val dbPath: String = sys.env(EnvVar.databasePath)
}

object Config {
  def load[F[_]](implicit F: Sync[F]): F[Config] = F.delay(new Config())

  object EnvVar {
    val galleryDir = "GALLERY_PATH"
    val databasePath = "DB_PATH"
  }
}
