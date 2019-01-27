package com.ilyshav.gallery

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import com.ilyshav.gallery.HttpModels.{AlbumContent, AlbumId}
import com.ilyshav.gallery.PrivateModels.Album
import com.ilyshav.gallery.process.Scanner
import org.http4s.server.Router

class GalleryService(config: Config, db: Database[IO])(
    implicit timer: Timer[IO], e: ConcurrentEffect[IO], s: ContextShift[IO]) {
  def start(): fs2.Stream[IO, Unit] = {
    fs2.Stream.emits(List(
      fullScan(),
      httpService()
    )).parJoinUnbounded
  }

  private def fullScan(): fs2.Stream[IO, Unit] =
    for {
      path <- fs2.Stream.eval(IO(config.galleryDir))
      _ <- Scanner.stream(path, db)
    } yield ()

  private def httpService(): fs2.Stream[IO, Unit] = {
    import org.http4s.implicits._
    import cats.effect._, org.http4s._, org.http4s.dsl.io._ // todo simplify
    import org.http4s.server.blaze._

    import Encoders._
    import org.http4s.circe.CirceEntityEncoder._

    val routes = HttpRoutes.of[IO] {
      case GET -> Root => Ok("hi there")
      case GET -> Root / "albums" =>
        Ok(getAlbum(None))
      case GET -> Root / "albums" / albumId =>
        Ok(getAlbum(Some(AlbumId(albumId))))
    }

    val httpApp = Router("/api" -> routes).orNotFound
    val serverBuilder =
      BlazeServerBuilder[IO].bindHttp(8080, "localhost").withHttpApp(httpApp)
    serverBuilder.serve.map(_ => ())
  }

  private def getAlbum(id: Option[AlbumId]): IO[AlbumContent] = {
    import cats.syntax.parallel._

    val albumToLoad = id.getOrElse(Album.root.id)

    val albums = db.getAlbums(albumToLoad)
    val photos = db.getPhotos(albumToLoad)

    (albums, photos).parMapN { case (albums, photos) =>
      AlbumContent(albums.map(_.toDto()), photos.map(_.toDto()))
    }
  }
}
