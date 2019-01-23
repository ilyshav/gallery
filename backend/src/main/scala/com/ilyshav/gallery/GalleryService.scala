package com.ilyshav.gallery

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import com.ilyshav.gallery.process.SearchAlbums
import org.http4s.server.Router

class GalleryService(config: Config, db: Database[IO])(
    implicit timer: Timer[IO], e: ConcurrentEffect[IO], cs: ContextShift[IO]) {
  def start(): fs2.Stream[IO, Unit] = {
    fs2.Stream.emits(List(
      fullAlbumsScan(),
      httpService()
    )).parJoinUnbounded
  }

  private def fullAlbumsScan(): fs2.Stream[IO, Unit] =
    for {
      path <- fs2.Stream.eval(IO(config.galleryDir))
      result <- SearchAlbums.fullScan(path, db).map(_ => ())
    } yield result

  private def httpService()(implicit cs: ContextShift[IO]): fs2.Stream[IO, Unit] = {
    import org.http4s.implicits._
    import cats.effect._, org.http4s._, org.http4s.dsl.io._ // todo simplify
    import org.http4s.server.blaze._

    import Encoders._
    import org.http4s.circe.CirceEntityEncoder._

    val routes = HttpRoutes.of[IO] {
      case GET -> Root => Ok("hi there")
      case GET -> Root / "albums" => Ok(db.getAlbums().map(_.map(_.toDto())))
    }

    val httpApp = Router("/api" -> routes).orNotFound
    val serverBuilder =
      BlazeServerBuilder[IO].bindHttp(8080, "localhost").withHttpApp(httpApp)
    serverBuilder.serve.map(_ => ())
  }

}
