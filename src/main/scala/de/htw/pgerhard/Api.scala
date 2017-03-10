package de.htw.pgerhard

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.config.ConfigFactory
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.marshalling.sprayJson._
import sangria.parser.QueryParser
import spray.json._

import scala.util.{Failure, Success}

object Api extends App {

  val environment = new DefaultEnvironment

  import environment.actorSystem
  import environment.actorMaterializer
  import environment.executionContext

  private def graphQLEndpoint(requestJson: JsValue) = {
    val JsObject(fields) = requestJson
    val JsString(query) = fields("query")

    val operation = fields.get("operationName") collect {
      case JsString(op) ⇒ op
    }

    val vars = fields.get("variables") match {
      case Some(obj: JsObject) ⇒ obj
      case _ ⇒ JsObject.empty
    }

    QueryParser.parse(query) match {
      case Success(queryAst) ⇒
        complete(
          Executor.execute(
            GraphQlSchema.schema,
            queryAst,
            environment,
            variables = vars,
            operationName = operation,
            deferredResolver = DeferredResolver.fetchers(GraphQlSchema.tweets, GraphQlSchema.users))
          .map(OK → _)
          .recover {
            case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
            case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
          })

      case Failure(error) ⇒
        complete(BadRequest, JsObject("error" → JsString(error.getMessage)))
    }
  }

  val graphiQlRoute = get { getFromResource("graphiql.html") }

  val graphQlRoute: Route =
    (post & path("graphql")) {
      entity(as[JsValue]) { requestJson ⇒
        graphQLEndpoint(requestJson)
      }
    } ~ graphiQlRoute

  val wsConf = ConfigFactory.load().getConfig("ws")
  Http().bindAndHandle(graphQlRoute, wsConf.getString("host"), wsConf.getInt("port"))
}
