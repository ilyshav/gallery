package com.ilyshav.gallery

import io.circe.Encoder

object Models {
  case class Album(name: String)
}

object Encoders {
  import io.circe.generic.semiauto._
  import Models._

  implicit val albumEncoder: Encoder[Album] = deriveEncoder
}