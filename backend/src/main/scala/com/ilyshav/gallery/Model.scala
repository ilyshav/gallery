package com.ilyshav.gallery

import java.nio.file.Path

import com.ilyshav.gallery.HttpModels.{AlbumDto, AlbumId, PhotoDto, PhotoId}
import io.circe.Encoder

object HttpModels {
  case class AlbumId(id: String) extends AnyVal
  case class PhotoId(id: String) extends AnyVal

  case class AlbumDto(id: AlbumId, name: String)
  case class PhotoDto(id: PhotoId, name: String)
}

object PrivateModels {
  case class Album(id: AlbumId, path: String, name: String, parent: Option[AlbumId]) {
    def toDto() = AlbumDto(id, name)
    def fullPath(root: Path): Path = root.resolve(path)
  }
  case class Photo(id: PhotoId, path: String) {
    def toDto() = PhotoDto(id, path) // todo name
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
}