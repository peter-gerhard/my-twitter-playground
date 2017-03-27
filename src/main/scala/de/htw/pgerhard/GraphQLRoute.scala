package de.htw.pgerhard

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, HandledException, QueryAnalysisError}
import sangria.marshalling.sprayJson._
import sangria.parser.QueryParser
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class GraphQLRoute(
    private val environment: Environment)(
  implicit
    private val ec: ExecutionContext) {

  private val exceptionHandler: Executor.ExceptionHandler = {
    case (m, e) => HandledException(s"There was an internal server error. ${e.getMessage}")
  }

  private val executor = Executor(
    GraphQLSchema.schema,
    deferredResolver = DeferredResolver.fetchers(
      GraphQLSchema.tweets,
      GraphQLSchema.users,
      GraphQLSchema.userTimelines,
      GraphQLSchema.homeTimelines),
    exceptionHandler = exceptionHandler)

  private def graphQLEndpoint(requestJson: JsValue) = {
    val JsObject(fields) = requestJson
    val JsString(query) = fields("query")

    val operation = fields.get("operationName") collect {
      case JsString(op) ⇒ op
    }

    val variables = fields.get("variables") match {
      case Some(obj: JsObject) ⇒ obj
      case _ ⇒ JsObject.empty
    }

    executeQuery(query, operation, variables)
  }

  private def executeQuery(query: String, operation: Option[String], variables: JsObject) = {
    QueryParser.parse(query) match {
      case Success(queryAst) ⇒
        complete(
          executor.execute(
            queryAst,
            environment,
            root = (),
            variables =variables,
            operationName = operation)
          .map(OK → _)
          .recover {
            case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
            case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
          })

      case Failure(error) ⇒
        complete(BadRequest, JsObject("error" → JsString(error.getMessage)))
    }
  }

  val route: Route =
    (post & path("graphql")) {
      entity(as[JsValue]) { requestJson ⇒
        graphQLEndpoint(requestJson)
      }
    }
}
