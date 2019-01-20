package com.ilyshav.gallery

import cats.effect.{ExitCode, IO, IOApp}

object Gallery extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = for {
    config <- Config.load[IO]
    database <- Database.open[IO](config.dbPath)
    service = new GalleryService[IO](config)
  } yield ExitCode.Success
}
