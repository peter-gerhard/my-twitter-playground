package de.htw.pgerhard

import com.github.agourlay.cornichon.CornichonFeature
import com.github.agourlay.cornichon.core.Step
import sangria.macros._
import scala.concurrent.duration._

trait MyTwitterSteps {
  this: CornichonFeature ⇒

  def query_user(id: String): Step =
    AttachAs(s"query user '$id'.") {

      When I query_gql("/graphql")
        .withVariables(
          "userId" → id)
        .withQuery(
          graphql"""
            query QueryUser($$userId: String!) {
              user(userId: $$userId) {
                id
                handle
                name
                following { id },
                followers { id }
              }
            }
          """)

      And assert status.is(200)
    }

  def register_a_user(handle: String, name: String, alias: String = "user-id"): Step =
    AttachAs(s"register a user with handle '$handle'") {

      When I query_gql("/graphql")
        .withVariables(
          "handle" → handle,
          "name" → name)
        .withQuery(
          graphql"""
            mutation RegisterUser($$handle: String!, $$name: String!) {
              registerUser(handle: $$handle, name: $$name) {
                id
              }
            }
          """)

      And assert status.is(200)

      And I save_body_path("data.registerUser.id" → alias)
      And I save_body_path("data.registerUser.id" → s"$alias-id")
    }

  def delete_user(id: String): Step =
    AttachAs(s"delete user '$id'.") {
      When I query_gql("/graphql")
        .withVariables(
          "userId" → id)
        .withQuery(
          graphql"""
            mutation DeleteUser($$userId: String!) {
              deleteUser(userId: $$userId)
            }
          """)

      And assert status.is(200)
    }

  def query_timeline_for_user(id: String): Step =
    AttachAs(s"query timeline for user '$id'.") {
      When I query_gql("/graphql")
        .withVariables(
          "userId" → id)
        .withQuery(
          graphql"""
            query QueryTimeline($$userId: String!) {
              timeline(userId: $$userId) {
                id
                userId
                tweets {
                  ... on Retweet {
                    tweet {
                      ... tweetFields
                    }
                    user { id }
                  }

                  ... on TweetRef {
                    tweet {
                      ... tweetFields
                    }
                  }

                }
              }
            }

            fragment tweetFields on Tweet {
              id
              author {
                id
              }
              retweetCount
            }
          """)

      And assert status.is(200)
    }

  def query_home_timeline_for_user(id: String): Step =
    AttachAs(s"query home timeine for user '$id'.") {
      When I query_gql("/graphql")
        .withVariables(
          "userId" → id)
        .withQuery(
          graphql"""
            query QueryHomeTimeline($$userId: String!) {
              homeTimeline(userId: $$userId) {
                author { id }
                body
              }
            }
          """)

      And assert status.is(200)
    }

  case class user(userId: String) {

    def setsName(name: String): Step =
      AttachAs(s"user '$userId' sets its name to '$name'.") {
        When I query_gql("/graphql")
          .withVariables(
            "userId" → userId,
            "name" → name)
          .withQuery(
            graphql"""
              mutation SetUserName($$userId: String!, $$name: String!) {
                setUserName(userId: $$userId, name: $$name) {
                  name
                }
              }
            """)

        And assert status.is(200)
    }

    def follows(followingId: String): Step =
      AttachAs(s"user '$userId' follows user '$followingId'.") {
        When I query_gql("/graphql")
          .withVariables(
            "userId" → userId,
            "followingId" → followingId)
          .withQuery(
            graphql"""
              mutation FollowUser($$userId: String!, $$followingId: String!) {
                followUser(userId: $$userId, followingId: $$followingId) {
                  following { id }
                }
              }
            """)

        And assert status.is(200)
    }

    def unfollows(followingId: String): Step =
      AttachAs(s"user '$userId' unfollows user '$followingId'.") {
        When I query_gql("/graphql")
          .withVariables(
            "userId" → userId,
            "followingId" → followingId)
          .withQuery(
            graphql"""
              mutation UnfollowUser($$userId: String!, $$followingId: String!) {
                unfollowUser(userId: $$userId, followingId: $$followingId) {
                  following { id }
                }
              }
            """)

        And assert status.is(200)
    }

    def tweets(body: String): Step =
      AttachAs(s"user '$userId' posts a tweet") {
        When I query_gql("/graphql")
          .withVariables(
            "userId" → userId,
            "body"→ body)
          .withQuery(
            graphql"""
              mutation PostTweet($$userId: String!, $$body: String!) {
                postTweet(userId: $$userId, body: $$body) {
                  id
                  author { id }
                  body
                  timestamp
                  likeCount
                  retweetCount
                }
              }
            """)

        And assert status.is(200)

        And I save_body_path("data.postTweet.id" → "tweet")
        And I save_body_path("data.postTweet.id" → "tweet-id")
      }

    def deletes_tweet(tweetId: String): Step =
      AttachAs(s"user '$userId' deletes tweet '$tweetId'.") {
        When I query_gql("/graphql")
          .withVariables(
            "userId" → userId,
            "tweetId" → tweetId)
          .withQuery(
            graphql"""
              mutation DeleteTweet($$userId: String!, $$tweetId: String!) {
                deleteTweet(userId: $$userId, tweetId: $$tweetId)
              }
            """)

        And assert status.is(200)
      }

    def retweets(tweetId: String): Step =
      AttachAs(s"user '$userId retweets tweet '$tweetId'") {
        When I query_gql("/graphql")
          .withVariables(
            "userId" → userId,
            "tweetId" → tweetId)
          .withQuery(
            graphql"""
              mutation PostRetweet($$userId: String!, $$tweetId: String!) {
                postRetweet(userId: $$userId, tweetId: $$tweetId) {
                  tweet { id }
                  user { id }
                }
              }
            """)

        And assert status.is(200)
      }

    def deletes_retweet(tweetId: String): Step =
      AttachAs(s"user '$userId deleted retweet of tweet '$tweetId'") {
        When I query_gql("/graphql")
          .withVariables(
            "userId" → userId,
            "tweetId" → tweetId)
          .withQuery(
            graphql"""
              mutation DeleteRetweet($$userId: String!, $$tweetId: String!) {
                deleteRetweet(userId: $$userId, tweetId: $$tweetId)
              }
            """)

        And assert status.is(200)
      }
  }

  def eventually = Eventually(maxDuration = 10.seconds, interval = 1.second)

  // Todo add possibility to register custom Matchers in MatcherResolver
//  val anyLong = Matcher(
//    key = "any-long",
//    description = "checks if the field is a long",
//    predicate = _.asNumber.flatMap(_.toLong).nonEmpty)
}
