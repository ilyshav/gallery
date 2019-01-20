package com.ilyshav.gallery

import java.util.concurrent.Executors

import cats.Monad
import cats.effect.{Async, Bracket, ContextShift, Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import doobie.implicits.toSqlInterpolator
import doobie.implicits.toConnectionIOOps

class Database[F[_]: Monad](transactor: Resource[F, HikariTransactor[F]])(implicit val br: Bracket[F, Throwable]) {
  def saveAlbum(path: String, checkTimestamp: Long): F[Unit] = {
    val sql =
      sql"""
           |insert into albums(path, lastCheck) values ($path, $checkTimestamp)
           | on conflict do update set lastCheck=$checkTimestamp
         """.stripMargin

    transactor.use(tx => sql.update.run.transact(tx)).map(_ => ())
  }
}

object Database {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def open[F[_]: Async: ContextShift](dbPath: String): F[Database[F]] =
    for {
      _ <- applyMigrations(dbPath)
      transactor <- openTransactor[F](dbPath)
    } yield new Database[F](transactor)

  private def applyMigrations[F[_]](dbPath: String)(
      implicit F: Sync[F]): F[Unit] =
    for {
      migrator <- F.delay {
        Flyway.configure
          .dataSource(s"jdbc:sqlite:file:$dbPath", null, null)
          .locations("classpath:db.migrations")
          .load()
      }
      appliedMigrations <- F.delay(migrator.migrate())
      _ <- F.delay(logger.info(s"Applied $appliedMigrations migrations."))
    } yield ()

  private def openTransactor[F[_]: ContextShift](dbPath: String)(
      implicit B: Bracket[F, Throwable],
      F: Async[F]): F[Resource[F, HikariTransactor[F]]] = {

    for {
        ec <- B
          .bracket(F.delay(Executors.newFixedThreadPool(5)))(ec => F.delay(ec))(
            ec => F.delay(ec.shutdown()))
          .map(ExecutionContext.fromExecutor)
      } yield
        HikariTransactor
          .newHikariTransactor[F](
            driverClassName = "org.sqlite.JDBC",
            url = s"jdbc:sqlite:file:$dbPath",
            user = "",
            pass = "",
            connectEC = ec,
            transactEC = ec
          )
  }
}
