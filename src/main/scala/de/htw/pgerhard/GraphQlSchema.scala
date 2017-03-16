package de.htw.pgerhard

import de.htw.pgerhard.domain.timeline.UserTimeline
import de.htw.pgerhard.domain.tweets.Tweet
import de.htw.pgerhard.domain.users.User
import de.htw.pgerhard.util.FutureOption
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{Field, _}

import scala.concurrent.Future

object GraphQlSchema {

  /**
    * Resolves the lists of tweets. These resolutions are batched and
    * cached for the duration of a query.
    */
  val tweets: Fetcher[Environment, Tweet, String] =
    Fetcher.caching((ctx: Environment, ids: Seq[String]) ⇒ ctx.tweets.getMultipleByIds(ids))(HasId(_.id))

  val users: Fetcher[Environment, User, String] =
    Fetcher.caching((ctx: Environment, ids: Seq[String]) ⇒ ctx.users.getMultipleByIds(ids))(HasId(_.id))

  lazy val TweetType: ObjectType[Environment, Tweet] =
    ObjectType(
      "Tweet",
      "A post on Twitter",
      () ⇒ fields[Environment, Tweet](
        Field("id", StringType,
          Some("The id of the tweet."),
          resolve = _.value.id),
        Field("author", OptionType(UserType),
          Some("The id of the author."),
          resolve = ctx ⇒ ctx.ctx.users.getById(ctx.value.authorId)),
        Field("body", StringType,
          Some("The body of the tweet."),
          resolve = _.value.body),
        Field("timestamp", LongType,
          Some("The time when the tweet was tweeted."),
          resolve = _.value.timestamp),
        Field("likeCount", IntType,
          Some("The number of people who liked this tweet."),
          resolve = _.value.likers.size),
        Field("retweetCount", IntType,
          Some("The number of people who retweeted this tweet."),
          resolve = _.value.retweeters.size)))

  lazy val UserType: ObjectType[Environment, User] =
    ObjectType(
      "User",
      "A Twitter User",
      () ⇒ fields[Environment, User](
        Field("id", StringType,
          Some("The id of the user."),
          resolve = _.value.id),
        Field("handle", StringType,
          Some("The twitter handle of the user."),
          resolve = _.value.handle),
        Field("name", StringType,
          Some("The name of the user."),
          resolve = _.value.name),
        Field("following", ListType(UserType),
          Some("The users this user is following."),
          resolve = ctx ⇒ users.deferSeq(ctx.value.following.toList)),
        Field("followers", ListType(UserType),
          Some("The users who follow this user."),
          resolve = ctx ⇒ users.deferSeq(ctx.value.followers.toList))))

  val TweetIdArg = Argument("tweetId", StringType, description = "Id of the tweet")
  val AuthorArg = Argument("authorId", StringType, description = "Id of the author")
  val UserArg = Argument("userId", StringType, description = "Id of the user")
  val BodyArg = Argument("body", StringType, description = "The body of the tweet")

  val UserIdArg = Argument("userId", StringType, description = "Id of the subject user")
  val followingIdArg = Argument("followingId", StringType, description = "Id of the user to follow")
  val HandleArg = Argument("handle", StringType, description = "handle of the user")
  val NameArg = Argument("name", StringType, description = "name of the user")

  val UserTimelineIdArg = Argument("timelineId", StringType, description = "Id of the user timeline")

  val QueryType = ObjectType(
    "Query", fields[Environment, Unit](
      Field("tweet", OptionType(TweetType),
        arguments = TweetIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.tweets.getById(ctx.arg(TweetIdArg))),
      Field("user", OptionType(UserType),
        arguments = UserIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.getById(ctx.arg(UserIdArg)))))

  val MutationType = ObjectType(
    "Mutation", fields[Environment, Unit](
      // UserTimeline Mutations
      Field("postTweet", OptionType(TweetType),
        arguments = UserTimelineIdArg :: AuthorArg :: BodyArg :: Nil,
        resolve = (ctx) ⇒ {
          import ctx.ctx.executionContext
          (for {
            tweet ← FutureOption(ctx.ctx.tweets.create(ctx.arg(AuthorArg), ctx.arg(BodyArg)))
            _     ← FutureOption(ctx.ctx.userTimelines.postTweet(ctx.arg(UserTimelineIdArg), tweet.id))
          } yield tweet).get
        }
      ),
      Field("deleteTweet", OptionType(TweetType),
        arguments = UserTimelineIdArg :: TweetIdArg :: Nil,
        resolve = (ctx) ⇒ {
          import ctx.ctx.executionContext
          (for {
            _     ← FutureOption(ctx.ctx.userTimelines.deleteTweet(ctx.arg(UserTimelineIdArg), ctx.arg(TweetIdArg)))
            tweet ← FutureOption(ctx.ctx.tweets.delete(ctx.arg(TweetIdArg)))
          } yield tweet).get
        }
      ),

      // User Mutations
      Field("registerUser", OptionType(UserType),
        arguments = HandleArg :: NameArg :: Nil,
        resolve = (ctx) ⇒ {
          import ctx.ctx.executionContext
          // move orchestration in connector!
          (for {
            user ← FutureOption(ctx.ctx.users.register(ctx.arg(HandleArg), ctx.arg(NameArg)))
            _    ← FutureOption(ctx.ctx.userTimelines.create(user.id))
          } yield user).get
        }
      ),
      Field("setUserName", OptionType(UserType),
        arguments = UserIdArg :: NameArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.setName(ctx.arg(UserIdArg), ctx.arg(NameArg))),
      Field("followUser", OptionType(UserType),
        arguments = UserIdArg :: followingIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.addToFollowing(ctx.arg(UserIdArg), ctx.arg(followingIdArg))),
      Field("unfollowUser", OptionType(UserType),
        arguments = UserIdArg :: followingIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.removeFromFollowing(ctx.arg(UserIdArg), ctx.arg(followingIdArg))),
      Field("deleteUser", OptionType(UserType),
        arguments = UserIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.delete(ctx.arg(UserIdArg)))))

//  val SubscriptionType = ObjectType("Subscription", fields[Environment, Unit](
//    Field.subs("test", TweetType,
//      arguments = Nil,
//      resolve = ctx ⇒ ctx
//    )
//  )


  val schema = Schema(QueryType, Some(MutationType))
}
