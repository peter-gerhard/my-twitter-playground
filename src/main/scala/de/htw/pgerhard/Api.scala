package de.htw.pgerhard

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.config.ConfigFactory

object Api extends App {

  val environment = new DefaultEnvironment

  import environment.actorSystem
  import environment.actorMaterializer
  import environment.executionContext

  private val wsConf = ConfigFactory.load().getConfig("ws")

  private val graphQLRoute: Route = new GraphQLRoute(environment).route
  private val graphiQLRoute: Route = get { getFromResource("graphiql.html") }

  Http().bindAndHandle(graphQLRoute ~ graphiQLRoute, wsConf.getString("host"), wsConf.getInt("port"))
}
