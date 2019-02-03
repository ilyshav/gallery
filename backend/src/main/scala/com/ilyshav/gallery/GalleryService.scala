package com.ilyshav.gallery

import java.io.File

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import com.ilyshav.gallery.HttpModels.{AlbumContent, AlbumId, PhotoId, ThumbnailId}
import com.ilyshav.gallery.PrivateModels.Album
import com.ilyshav.gallery.process.Scanner
import org.http4s.server.Router

import scala.concurrent.ExecutionContext

class GalleryService(config: Config,
                     db: Database[IO],
                     blockingEc: ExecutionContext)(implicit timer: Timer[IO],
                                                   e: ConcurrentEffect[IO],
                                                   s: ContextShift[IO]) {
  def start(): fs2.Stream[IO, Unit] = {
    fs2.Stream
      .emits(
        List(
          fullScan(),
          httpService()
        ))
      .parJoinUnbounded
  }

  private def fullScan(): fs2.Stream[IO, Unit] =
    Scanner.stream(config, db)

  private def httpService(): fs2.Stream[IO, Unit] = {
    import org.http4s.implicits._
    import cats.effect._, org.http4s._, org.http4s.dsl.io._ // todo simplify
    import org.http4s.server.blaze._

    import Encoders._
    import org.http4s.circe.CirceEntityEncoder._

    val staticRoutes = HttpRoutes.of[IO] {
      case r @ GET -> Root / "static" / "photo" / id =>
        db.getPhoto(PhotoId(id))
          .flatMap(_.fold(NotFound()) { photo =>
            StaticFile
              .fromFile(new File(photo.path), blockingEc, Some(r))
              .getOrElseF(NotFound())
          })
      case r @ GET -> Root / "static" / "thumbnail" / id =>
        db.getThumbnail(ThumbnailId(id))
          .flatMap(_.fold(NotFound()) { thumbnail =>
            StaticFile
              .fromFile(new File(thumbnail.path), blockingEc, Some(r))
              .getOrElseF(NotFound())
          })
      case r @ GET -> Root =>
        StaticFile
          .fromResource(s"/frontend/index.html", blockingEc, Some(r), true)
          .getOrElseF(NotFound())
      case r =>
        StaticFile.fromResource(s"/frontend${r.pathInfo}", blockingEc, Some(r), true)
          .getOrElseF(NotFound())
    }

    val apiRoutes = HttpRoutes.of[IO] {
      case GET -> Root / "albums" =>
        Ok(getAlbum(None))
      case GET -> Root / "albums" / albumId =>
        Ok(getAlbum(Some(AlbumId(albumId))))
    }

    val httpApp = Router("/" -> staticRoutes, "/api" -> apiRoutes).orNotFound
    val serverBuilder =
      BlazeServerBuilder[IO].bindHttp(config.port, "0.0.0.0").withHttpApp(httpApp)
    serverBuilder.serve.map(_ => ())
  }

  private def getAlbum(id: Option[AlbumId]): IO[AlbumContent] = {
    import cats.syntax.parallel._

    val albumToLoad = id.getOrElse(Album.root.id)

    val albums = db.getAlbums(albumToLoad)
    val photos = db.getPhotos(albumToLoad)

    (albums, photos).parMapN {
      case (albums, photos) =>
        AlbumContent(albums.map(_.toDto()), photos.map(_.toDto()))
    }
  }
}
