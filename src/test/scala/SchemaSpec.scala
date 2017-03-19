import de.htw.pgerhard.{DefaultEnvironment, Environment, GraphQlSchema}
import org.scalatest.{Matchers, WordSpec}
import sangria.macros._
import sangria.ast.Document
import sangria.execution.{Executor, HandledException}
import sangria.marshalling.sprayJson._

import scala.concurrent.Await
import scala.concurrent.duration._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global

class SchemaSpec extends WordSpec with Matchers {

  "MySchema" should {
    "Register a user" in {

      val query =
        graphql"""
         mutation {
           registerUser(handle: "@pgerhard", name: "Peter Gerhard") {
             handle
             name
           }
         }
       """

      executeQuery(query) should be (
        """
         {
           "data": {
             "registerUser": {
               "handle": "@pgerhard",
               "name": "Peter Gerhard"
             }
           }
         }
       """.parseJson)
    }

    "Return with Usernot found error" in {
      val query =
        graphql"""
         query {
           user(userId: "") {
             id
           }
         }
       """

      executeQuery(query) should be (
        """
         {
           "data": null,
           "errors": [{
             "message": "User 'dadadasd' not found.",
             "path":["user"],
             "locations":[{
               "line":3,
               "column":12
             }]
           }]
         }
       """.parseJson)
    }
  }

  private def executeQuery(query: Document, vars: JsObject = JsObject.empty): JsValue = {
    val futureResult = Executor.execute(GraphQlSchema.schema, query,
      variables = vars,
      userContext = new DefaultEnvironment)

    Await.result(futureResult, 10.seconds)
  }
}
