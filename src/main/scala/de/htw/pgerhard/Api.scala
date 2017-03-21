package de.htw.pgerhard

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkasse.ServerSentEvent
import de.heikoseeberger.akkasse.EventStreamMarshalling.toEventStream
import sangria.ast.OperationType
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, HandledException, QueryAnalysisError}
import sangria.marshalling.sprayJson._
import sangria.parser.QueryParser
import spray.json._

import scala.util.control.NonFatal
import scala.util.{Failure, Success}

object Api extends App {

  val environment = new DefaultEnvironment

  import environment.actorSystem
  import environment.actorMaterializer
  import environment.executionContext


  val exceptionHandler: Executor.ExceptionHandler = {
    case (m, e) => HandledException(s"There was an internal server error. ${e.getMessage} ")
  }

  val executor = Executor(
    GraphQlSchema.schema,
    deferredResolver = DeferredResolver.fetchers(GraphQlSchema.tweets, GraphQlSchema.users),
    exceptionHandler = exceptionHandler)

  private def executeQuery(query: String, operation: Option[String], variables: JsObject) = {
    QueryParser.parse(query) match {
      case Success(queryAst) ⇒
        queryAst.operationType(operation) match {
          case Some(OperationType.Subscription) ⇒
            import sangria.execution.ExecutionScheme.Stream
            import sangria.streaming.akkaStreams._
            complete(
              executor.prepare(queryAst, environment, (), operation, variables)
                .map { preparedQuery ⇒
                  ToResponseMarshallable(
                    preparedQuery.execute()
                      .map(result ⇒ ServerSentEvent(result.compactPrint))
                      .recover {
                        case NonFatal(error) ⇒
                          // Todo Log error
                          ServerSentEvent(error.getMessage)
                      })
                }
                .recover {
                  case error: QueryAnalysisError ⇒ ToResponseMarshallable(BadRequest → error.resolveError)
                  case error: ErrorWithResolver ⇒ ToResponseMarshallable(InternalServerError → error.resolveError)
                })
          case _ ⇒
            complete(
              executor.execute(
                queryAst,
                environment,
                root = (),
                operationName = operation,
                variables = variables)
              .map(OK → _)
              .recover {
                case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
                case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
              })
        }
      case Failure(error) ⇒
        complete(BadRequest, JsObject("error" → JsString(error.getMessage)))
    }
  }

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
