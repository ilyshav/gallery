package com.ilyshav.gallery

import java.util.concurrent.Executors

import cats.effect.{Bracket, ExitCode, IO, IOApp}
import doobie.hikari.HikariTransactor

import scala.concurrent.ExecutionContext

object Gallery extends IOApp {
  val B = implicitly[Bracket[IO, Throwable]]

  override def run(args: List[String]): IO[ExitCode] =
    for {
      config <- Config.load[IO]
      r <- B.bracket(IO(Executors.newCachedThreadPool())) { executor =>
        val executionContext = ExecutionContext.fromExecutor(executor)
        HikariTransactor
          .newHikariTransactor[IO](
            driverClassName = "org.sqlite.JDBC",
            url = s"jdbc:sqlite:file:${config.dbPath}",
            user = "",
            pass = "",
            connectEC = executionContext,
            transactEC = executionContext
          )
          .use { transactor =>
            B.bracket(IO(Executors.newFixedThreadPool(2))) { executor =>
              val blockingEc = ExecutionContext.fromExecutor(executor)
              for {
                config <- Config.load[IO]
                database <- Database.open[IO](config, transactor)
                service = new GalleryService(config, database, blockingEc)
                _ <- service.start().compile.drain
              } yield ExitCode.Success
            } (x => IO(x.shutdown()))
          }
      }(executor => IO(executor.shutdown()))
    } yield r

}
