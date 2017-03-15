package de.htw.pgerhard

import com.github.agourlay.cornichon.CornichonFeature
import com.github.agourlay.cornichon.resolver.{JsonMapper, Mapper}

import scala.concurrent.duration._
import scala.language.postfixOps

trait MyTwitterFeature extends CornichonFeature with WithWsConfig {

  override lazy val baseUrl: String = wsConfig.serviceUrl

  override lazy val requestTimeout: FiniteDuration = 4 seconds
}