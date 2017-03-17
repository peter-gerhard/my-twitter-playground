import de.htw.pgerhard.{DefaultEnvironment, Environment}
import org.scalatest.{Matchers, WordSpec}
import sangria.macros._
import sangria.ast.Document
import sangria.execution.Executor
import sangria.marshalling.sprayJson._

import scala.concurrent.Await
import scala.concurrent.duration._
import spray.json._
import sangria.schema._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Try}

object GraphQlSchema2 {

  def res: Future[Try[String]] = Future(Failure(StringError("haha")))

  case class StringError(reason: String) extends Throwable

  val QueryType = ObjectType(
    "Query", fields[Environment, Unit](
      Field("test", StringType,
        arguments = Nil,
        resolve = _ ⇒ res.map(_.get)
      )
    ))

  val schema = Schema(QueryType)
}

class SchemaSpec extends WordSpec with Matchers {

  "MySchema" should {
    "do something with Either" in {

      val query =
        graphql"""
         query {
           test
         }
       """

      executeQuery(query) should be (
        """
         {
           "data": {
             "test": null
           }
         }
       """.parseJson)
    }
  }

  private def executeQuery(query: Document, vars: JsObject = JsObject.empty) = {
    val futureResult = Executor.execute(GraphQlSchema2.schema, query,
      variables = vars,
      userContext = new DefaultEnvironment)

    Await.result(futureResult, 10.seconds)
  }
}
