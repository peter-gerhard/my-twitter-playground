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

      And the user("<user2>").retweets("<tweet[3]>")

      eventually {
        When I query_home_timeline_for_user("<test-user>")

        Then assert body.path("data.homeTimeline.tweets").asArray.inOrder.is(
          """
            |  tweet                    |  author                |  reposter             |
            |  {"id": "<tweet-id[3]>"}  |  {"id": "<user3-id>"}  | {"id": "<user2-id>"}  |
            |  {"id": "<tweet-id[3]>"}  |  {"id": "<user3-id>"}  | null                  |
            |  {"id": "<tweet-id[2]>"}  |  {"id": "<user1-id>"}  | null                  |
            |  {"id": "<tweet-id[1]>"}  |  {"id": "<user2-id>"}  | null                  |
            |  {"id": "<tweet-id[0]>"}  |  {"id": "<user1-id>"}  | null                  |
          """)
      }
    }
  }
}
