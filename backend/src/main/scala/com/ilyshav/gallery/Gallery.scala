package com.ilyshav.gallery

import java.util.concurrent.Executors

import cats.effect.{Bracket, ExitCode, IO, IOApp}

object Gallery extends IOApp {
  val B = implicitly[Bracket[IO, Throwable]]

  override def run(args: List[String]): IO[ExitCode] = B.bracket(IO(Executors.newCachedThreadPool())) { executor =>
    for {
      config <- Config.load[IO]
      database <- Database.open[IO](config, executor)
      service = new GalleryService[IO](config, database)
      _ <- service.start()
    } yield ExitCode.Success
  } (executor => IO(executor.shutdown()))
}
