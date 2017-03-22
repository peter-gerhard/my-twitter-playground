package de.htw.pgerhard
import com.github.agourlay.cornichon.core.FeatureDef

class HomeTimelineFeature extends MyTwitterFeature with MyTwitterSteps with WithMultipleUsers {

  override def feature: FeatureDef = Feature("HomeTimeline Feature") {

    Scenario("Deliver home timeline for a user") {

      When the user("<test-user>").follows("<user1>")

      And the user("<test-user>").follows("<user2>")

      And the user("<test-user>").follows("<user3>")

      And the user("<user1>").tweets("Hello World 1")

      And the user("<user2>").tweets("Hello World 2")

      And the user("<user1>").tweets("Hello World 3")

      And the user("<user3>").tweets("Hello World 4")

      When I query_home_timeline_for_user("<test-user>")

      And I show_last_response_json

      Then assert body.path("data.homeTimeline").asArray.is(
        """
        |  author                |  body            |
        |  {"id": "<user1-id>"}  | "Hello World 1"  |
        |  {"id": "<user2-id>"}  | "Hello World 2"  |
        |  {"id": "<user1-id>"}  | "Hello World 3"  |
        |  {"id": "<user3-id>"}  | "Hello World 4"  |
        """)
    }
  }
}
