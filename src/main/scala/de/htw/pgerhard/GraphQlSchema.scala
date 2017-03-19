package de.htw.pgerhard

import de.htw.pgerhard.domain.timeline.{Retweet, TweetRef, UserTimelineProjection, WithTweet}
import de.htw.pgerhard.domain.tweets.Tweet
import de.htw.pgerhard.domain.users.User
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

object GraphQlSchema {

  /**
    * Resolves the lists of tweets. These resolutions are batched and
    * cached for the duration of a query.
    */
  val tweets: Fetcher[Environment, Tweet, String] =
    Fetcher.caching((ctx: Environment, ids: Seq[String]) ⇒ ctx.tweets.getMultipleByIds(ids))(HasId(_.id))

  val users: Fetcher[Environment, User, String] =
    Fetcher.caching((ctx: Environment, ids: Seq[String]) ⇒ ctx.users.getMultipleByIds(ids))(HasId(_.id))

  lazy val UserTimelineType: ObjectType[Environment, UserTimelineProjection] =
    ObjectType(
      "UserTimeline",
      "A history of tweets and retweets of a user",
      () ⇒ fields[Environment, UserTimelineProjection](
        Field("id", StringType,
          Some(""),
          resolve = _.value.id),
        Field("userId", StringType,
          Some(""),
          resolve = _.value.userId),
        Field("tweets", ListType(WithTweetType),
          Some(""),
          resolve = _.value.tweets)))

  lazy val WithTweetType: InterfaceType[Environment, WithTweet] =
    InterfaceType(
      "WithTweet",
      "A retweet or a reference to a tweet",
      () ⇒ fields[Environment, WithTweet](
        Field("tweet", TweetType,
          Some(""),
          resolve = ctx ⇒ tweets.defer(ctx.value.tweetId))))

  lazy val RetweetType: ObjectType[Environment, Retweet] =
    ObjectType(
      "Retweet",
      "A retweet",
      interfaces[Environment, Retweet](WithTweetType),
      fields[Environment, Retweet](
        Field("tweet", TweetType,
          Some(""),
          resolve = ctx ⇒ tweets.defer(ctx.value.tweetId))))

  lazy val TweetRefType: ObjectType[Environment, TweetRef] =
    ObjectType(
      "TweetRef",
      "A Reference to a tweet",
      interfaces[Environment, TweetRef](WithTweetType),
      fields[Environment, TweetRef](
        Field("tweet", TweetType,
          Some(""),
          resolve = ctx ⇒ tweets.defer(ctx.value.tweetId))))

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
          resolve = ctx ⇒ users.defer(ctx.value.authorId)),
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

  val QueryType = ObjectType(
    "Query", fields[Environment, Unit](
      Field("tweet", TweetType,
        arguments = TweetIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.tweets.getById(ctx.arg(TweetIdArg))),
      Field("user", UserType,
        arguments = UserIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.getById(ctx.arg(UserIdArg)))))

  val MutationType = ObjectType(
    "Mutation", fields[Environment, Unit](
      // UserTimeline Mutations
      Field("postTweet", TweetType,
        arguments = UserIdArg :: AuthorArg :: BodyArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.userTimelines.postTweet(ctx.arg(UserIdArg), ctx.arg(BodyArg))),
      Field("deleteTweet", BooleanType,
        arguments = UserIdArg :: TweetIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.userTimelines.deleteTweet(ctx.arg(UserIdArg), ctx.arg(TweetIdArg))),
      Field("postRetweet", RetweetType,
        arguments = UserIdArg :: TweetIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.userTimelines.postRetweet(ctx.arg(UserIdArg), ctx.arg(TweetIdArg))),
      Field("deleteRetweet", BooleanType,
        arguments = UserIdArg :: TweetIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.userTimelines.deleteRetweet(ctx.arg(UserIdArg), ctx.arg(TweetIdArg))),

      // User Mutations
      Field("registerUser", UserType,
        arguments = HandleArg :: NameArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.register(ctx.arg(HandleArg), ctx.arg(NameArg))),
      Field("setUserName", OptionType(UserType),
        arguments = UserIdArg :: NameArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.setName(ctx.arg(UserIdArg), ctx.arg(NameArg))),
      Field("followUser", OptionType(UserType),
        arguments = UserIdArg :: followingIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.addToFollowing(ctx.arg(UserIdArg), ctx.arg(followingIdArg))),
      Field("unfollowUser", OptionType(UserType),
        arguments = UserIdArg :: followingIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.removeFromFollowing(ctx.arg(UserIdArg), ctx.arg(followingIdArg))),
      Field("deleteUser", BooleanType,
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
