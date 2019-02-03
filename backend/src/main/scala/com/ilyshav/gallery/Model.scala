package com.ilyshav.gallery

import java.io.File
import java.nio.file.{Path, Paths}

import com.ilyshav.gallery.HttpModels.{AlbumDto, AlbumId, PhotoDto, PhotoId}
import io.circe.Encoder


object HttpModels {
  case class AlbumId(id: String) extends AnyVal
  case class PhotoId(id: String) extends AnyVal
  case class ThumbnailId(id: String) extends AnyVal

  case class AlbumDto(id: AlbumId, name: String)
  case class PhotoDto(id: PhotoId, name: String)

  case class AlbumContent(albums: List[AlbumDto], photos: List[PhotoDto])
}

object PrivateModels {
  case class Album(id: AlbumId, path: String, name: String, parent: Option[AlbumId]) {
    def toDto() = AlbumDto(id, name)
    def fullPath(root: Path): Path = root.resolve(path)
  }
  case class Photo(id: PhotoId, path: String) {
    def toDto() = PhotoDto(id, path) // todo name
    def thumbnailPath(galleryRoot: Path, thumbnailsRoot: Path): File = {
      val photoPath = Paths.get(path)
      val relative = galleryRoot.relativize(photoPath)
      new File(s"$thumbnailsRoot/${relative.toString}")
    }
  }

  object Album {
    val root: Album = new Album(
      id = AlbumId("root")
      , path = ""
      , name = "root album"
      , parent = None
    )
  }
}

object Encoders {
  import io.circe.generic.extras.semiauto.deriveUnwrappedEncoder
  import io.circe.generic.semiauto._
  import HttpModels._

  implicit val albumIdEncoder: Encoder[AlbumId] = deriveUnwrappedEncoder
  implicit val photoIdEncoder: Encoder[PhotoId] = deriveUnwrappedEncoder

  implicit val albumEncoder: Encoder[AlbumDto] = deriveEncoder
  implicit val photoEncoder: Encoder[PhotoDto] = deriveEncoder

  implicit val albumContent: Encoder[AlbumContent] = deriveEncoder
}