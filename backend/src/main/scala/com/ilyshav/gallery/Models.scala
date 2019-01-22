package com.ilyshav.gallery

import io.circe.Encoder

object Models {
  case class AlbumId(id: Int) extends AnyVal

  case class Album(id: AlbumId, name: String)
}

object Encoders {
  import io.circe.generic.extras.semiauto.deriveUnwrappedEncoder
  import io.circe.generic.semiauto._
  import Models._

  implicit val albumIdEncoder: Encoder[AlbumId] = deriveUnwrappedEncoder
  implicit val albumEncoder: Encoder[Album] = deriveEncoder
}