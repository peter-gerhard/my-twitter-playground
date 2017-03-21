package de.htw.pgerhard

import com.github.agourlay.cornichon.core.FeatureDef

class TimelineFeature extends MyTwitterFeature with MyTwitterSteps with WithUser {

  override def feature: FeatureDef = Feature("Timeline Feature") {

    Scenario("Post a tweet") {

      When the user("<test-user>").tweets("Hi im using twitter")

      Then assert body.path("data.postTweet").whitelisting.is(
        """
        {
          "id": "<tweet-id>",
          "author": { "id": "<test-user-id>" },
          "body": "Hi im using twitter",
          "likeCount": 0,
          "retweetCount": 0
        }
        """)

      When I query_timeline_for_user("<test-user>")

      Then assert body.path("data.timeline.tweets").is(
        """
        [{
          "tweet": {
            "id": "<tweet-id>",
            "author": {
              "id": "<test-user>"
            },
            "retweetCount": 0
          }
        }]""")
    }

    Scenario("Delete a tweet") {

      When the user("<test-user>").tweets("Hi im using twitter")

      And I query_timeline_for_user("<test-user>")

      Then assert body.path("data.timeline.tweets").asArray.hasSize(1)

      When the user("<test-user>").deletes_tweet("<tweet>")

      eventually {
        When I query_timeline_for_user("<test-user>")

        Then assert body.path("data.timeline.tweets").asArray.isEmpty
      }
    }

    Scenario("Post a retweet") {

      When I register_a_user(handle = "@hwurst", name = "HansWurst", alias = "hans")

      And the user("<hans>").tweets("Hi im using twitter")

      Then assert body.path("data.postTweet.retweetCount").is(0)

      When the user("<test-user>").retweets("<tweet>")

      Then assert body.path("data.postRetweet").is(
        """
        {
          "tweet": {
            "id": "<tweet-id>"
          },
          "user": {
            "id": "<test-user-id>"
          }
        }
        """)

      When I query_timeline_for_user("<test-user>")

      Then assert body.path("data.timeline.tweets").is(
        """
        [{
          "tweet": {
            "id": "<tweet-id>",
            "author": {
              "id": "<hans-id>"
            },
            "retweetCount": 1
          },
          "user": {
            "id": "<test-user-id>"
          }
        }]""")

      And afterwards delete_user("<hans>")
    }

    Scenario("Delete a retweet") {

      When I register_a_user(handle = "@hwurst", name = "HansWurst", alias = "hans")

      And the user("<hans>").tweets("Hi im using twitter")

      And the user("<test-user>").retweets("<tweet>")

      When I query_timeline_for_user("<test-user>")

      Then assert body.path("data.timeline.tweets").asArray.hasSize(1)

      When I query_timeline_for_user("<hans>")

      Then assert body.path("data.timeline.tweets").is(
        """
        [{
          "tweet": {
            "id": "<tweet-id>",
            "author": {
              "id": "<hans-id>"
            },
            "retweetCount": 1
          }
        }]""")

      When the user("<test-user>").deletes_retweet("<tweet>")

      eventually {
        When I query_timeline_for_user("<test-user>")

        Then assert body.path("data.timeline.tweets").asArray.isEmpty
      }

      When I query_timeline_for_user("<hans>")

      Then assert body.path("data.timeline.tweets").is(
        """
        [{
          "tweet": {
            "id": "<tweet-id>",
            "author": {
              "id": "<hans-id>"
            },
            "retweetCount": 0
          }
        }]""")

      And afterwards delete_user("<hans>")
    }
  }
}
