package de.htw.pgerhard

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

trait WithWsConfig {
  implicit lazy val wsConfig: WsConfig = ConfigFactory.load().as[WsConfig]("ws")
}

case class WsConfig(serviceUrl: String)
