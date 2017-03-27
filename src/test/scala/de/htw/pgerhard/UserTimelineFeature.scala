package de.htw.pgerhard

import com.github.agourlay.cornichon.core.FeatureDef

class UserTimelineFeature extends MyTwitterFeature with MyTwitterSteps with WithUser {

  override def feature: FeatureDef = Feature("Timeline Feature") {

    Scenario("Post a tweet") {

      When the user("<test-user>").tweets("Hi im using twitter")

      Then assert body.path("data.postTweet").whitelisting.is(
        """
        {
          "id": "<tweet-id>",
          "author": { "id": "<test-user-id>" },
          "body": "Hi im using twitter"
        }
        """)

      eventually {
        When I query_timeline_for_user("<test-user>")

        Then assert body.path("data.userTimeline.tweets").whitelisting.is(
          """
          [{
            "tweet": {
              "id": "<tweet-id>"
            },
            "author": {
              "id": "<test-user-id>"
            }
          }]""")
      }
    }

    Scenario("Delete a tweet") {

      When the user("<test-user>").tweets("Hi im using twitter")

      eventually {
        When I query_timeline_for_user("<test-user>")

        Then assert body.path("data.userTimeline.tweets").asArray.hasSize(1)
      }

      When the user("<test-user>").deletes_tweet("<tweet>")

      eventually {
        When I query_timeline_for_user("<test-user>")

        Then assert body.path("data.userTimeline.tweets").asArray.isEmpty
      }
    }

    Scenario("Post a retweet") {

      When I register_a_user(handle = "@hwurst", name = "HansWurst", alias = "hans")

      And the user("<hans>").tweets("Hi im using twitter")

      eventually {
        When I query_tweet("<tweet-id>")

        Then assert body.path("data.tweet.repostCount").is(0)
      }

      When the user("<test-user>").retweets("<tweet>")

      eventually {
        When I query_tweet("<tweet-id>")

        Then assert body.path("data.tweet.repostCount").is(1)

        When I query_timeline_for_user("<test-user>")

        Then assert body.path("data.userTimeline.tweets").is(
          """
          [{
            "tweet": {
              "id": "<tweet-id>"
            },
            "author": {
              "id": "<hans-id>"
            },
            "reposter": {
              "id": "<test-user-id>"
            }
          }]""")
      }

      And afterwards delete_user("<hans>")
    }

    Scenario("Delete a retweet") {

      When I register_a_user(handle = "@hwurst", name = "HansWurst", alias = "hans")

      And the user("<hans>").tweets("Hi im using twitter")

      And the user("<test-user>").retweets("<tweet>")

      eventually {
        When I query_tweet("<tweet-id>")

        Then assert body.path("data.tweet.repostCount").is(1)

        When I query_timeline_for_user("<test-user>")

        Then assert body.path("data.userTimeline.tweets").is(
          """
          [{
            "tweet": {
              "id": "<tweet-id>"
            },
            "author": {
              "id": "<hans-id>"
            },
            "reposter": {
              "id": "<test-user-id>"
            }
          }]""")
      }

      When the user("<test-user>").deletes_retweet("<tweet>")

      eventually {
        When I query_tweet("<tweet-id>")

        Then assert body.path("data.tweet.repostCount").is(0)

        When I query_timeline_for_user("<test-user>")

        Then assert body.path("data.userTimeline.tweets").asArray.isEmpty
      }

      And afterwards delete_user("<hans>")
    }
  }
}
