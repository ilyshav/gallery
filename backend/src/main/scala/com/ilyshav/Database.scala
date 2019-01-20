package com.ilyshav

import cats.effect.{Async, ContextShift, Resource, Sync}
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.hikari.HikariTransactor
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

class Database[F[_]](transactor: Resource[F, HikariTransactor[F]]) {

}

object Database {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def open[F[_]: Async: ContextShift](dbPath: String): F[Database[F]] = for {
    _ <- applyMigrations(dbPath)
    transactor = openTransactor[F](dbPath)
  } yield new Database[F](transactor)

  private def applyMigrations[F[_]](dbPath: String)(implicit F: Sync[F]): F[Unit] = for {
    migrator <- F.delay {
      Flyway.configure.dataSource(s"jdbc:sqlite:file:$dbPath", null, null)
        .load()
    }
    appliedMigrations <- F.delay(migrator.migrate())
    _ <- F.delay(logger.info(s"Applied $appliedMigrations migrations."))
  } yield ()

  private def openTransactor[F[_]: Async: ContextShift](dbPath: String): Resource[F, HikariTransactor[F]] = {
    HikariTransactor.newHikariTransactor[F](
      driverClassName = "org.sqlite.JDBC",
      url = "jdbc:sqlite:file:$dbPath",
      user = "",
      pass = "",
      connectEC = ExecutionContext.global, // todo
      transactEC = ExecutionContext.global // todo
    )
  }
}
