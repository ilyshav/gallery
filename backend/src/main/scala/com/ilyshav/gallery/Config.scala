package com.ilyshav.gallery

import java.nio.file.{Files, Path, Paths}

import cats.effect.Sync

class Config private () {
  import Config._

  val galleryDir: Path = {
    val p = Paths.get(sys.env(EnvVar.galleryDir))
    if (Files.exists(p)) p
    else
      throw new RuntimeException(
        s"Albums dir (${sys.env(EnvVar.galleryDir)}) not exist")

  }
  val dbPath: String = sys.env(EnvVar.databasePath)
  val port: Int = sys.env.get(EnvVar.httpPort).map(_.toInt).getOrElse(80)
}

object Config {
  def load[F[_]](implicit F: Sync[F]): F[Config] = F.delay(new Config())

  object EnvVar {
    val galleryDir = "GALLERY_PATH"
    val databasePath = "DB_PATH"
    val httpPort = "HTTP_PORT"
  }
}
