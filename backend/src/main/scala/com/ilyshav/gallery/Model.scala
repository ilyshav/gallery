package com.ilyshav.gallery

import com.ilyshav.gallery.HttpModels.{AlbumDto, AlbumId, PhotoId}
import io.circe.Encoder

object HttpModels {
  case class AlbumId(id: String) extends AnyVal
  case class PhotoId(id: String) extends AnyVal

  case class AlbumDto(id: AlbumId, name: String)
}

object PrivateModels {
  case class Album(id: AlbumId, path: String, name: String) {
    def toDto() = AlbumDto(id, name)
  }
  case class Photo(id: PhotoId, fileName: String)
}

object Encoders {
  import io.circe.generic.extras.semiauto.deriveUnwrappedEncoder
  import io.circe.generic.semiauto._
  import HttpModels._

  implicit val albumIdEncoder: Encoder[AlbumId] = deriveUnwrappedEncoder
  implicit val albumEncoder: Encoder[AlbumDto] = deriveEncoder
}