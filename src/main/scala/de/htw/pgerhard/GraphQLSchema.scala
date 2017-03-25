package de.htw.pgerhard

import de.htw.pgerhard.domain.timelines.{HomeTimeline, TweetLike, UserTimeline}
import de.htw.pgerhard.domain.tweets.{SimpleTweet, Tweet}
import de.htw.pgerhard.domain.users.{SimpleUser, User}
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

object GraphQLSchema {

  /**
    * Resolves the lists of tweets. These resolutions are batched and
    * cached for the duration of a query.
    */
  val tweets: Fetcher[Environment, SimpleTweet, String] =
    Fetcher.caching((ctx: Environment, ids: Seq[String]) ⇒ ctx.tweets.getSeqByIds(ids))(HasId(_.id))

  val users: Fetcher[Environment, SimpleUser, String] =
    Fetcher.caching((ctx: Environment, ids: Seq[String]) ⇒ ctx.users.getSeqByIds(ids))(HasId(_.id))

  val userTimelines: Fetcher[Environment, UserTimeline, String] =
    Fetcher.caching((ctx: Environment, ids: Seq[String]) ⇒ ctx.userTimelines.getSeqByIds(ids))(HasId(_.userId))

  val homeTimelines: Fetcher[Environment, HomeTimeline, String] =
    Fetcher.caching((ctx: Environment, ids: Seq[String]) ⇒ ctx.homeTimelines.getSeqByIds(ids))(HasId(_.userId))

  lazy val TweetType: ObjectType[Environment, SimpleTweet] =
    ObjectType("Tweet", "A short message that can be posted on MyTwitter.",
      () ⇒ fields[Environment, SimpleTweet](
        Field("id", StringType,
          Some("The id of the tweet."),
          resolve = _.value.id),
        Field("author", UserType,
          Some("The user who posted the tweet."),
          resolve = ctx ⇒ users.defer(ctx.value.authorId)),
        Field("timestamp", LongType,
          Some("The timestamp when the tweet was posted."),
          resolve = _.value.timestamp),
        Field("body", StringType,
          Some("The content of the tweet."),
          resolve = _.value.body),
        Field("likeCount", IntType,
          Some("The number of users who liked this tweet."),
          resolve = _.value.likeCount),
        Field("repostCount", IntType,
          Some("The number of users who retweeted this tweet."),
          resolve = _.value.repostCount)))

  lazy val TweetCMType: ObjectType[Environment, Tweet] =
    ObjectType("Tweet", "The command model for Tweet.",
      () ⇒ fields[Environment, Tweet](
        Field("id", StringType,
          Some("The id of the tweet."),
          resolve = _.value.id),
        Field("author", UserType,
          Some("The user who posted the tweet."),
          resolve = ctx ⇒ users.defer(ctx.value.authorId)),
        Field("timestamp", LongType,
          Some("The timestamp when the tweet was posted."),
          resolve = _.value.timestamp),
        Field("body", StringType,
          Some("The content of the tweet."),
          resolve = _.value.body),
        Field("likedBy", ListType(StringType),
          Some("The number of users who liked this tweet."),
          resolve = _.value.likedBy.toSeq),
        Field("retpostedBy", ListType(StringType),
          Some("The number of users who retweeted this tweet."),
          resolve = _.value.repostedBy.toSeq)))

  lazy val UserType: ObjectType[Environment, SimpleUser] =
    ObjectType("User", "A MyTwitter user.",
      () ⇒ fields[Environment, SimpleUser](
        Field("id", StringType,
          Some("The id of the user."),
          resolve = _.value.id),
        Field("handle", StringType,
          Some("The MyTwitter handle of the user."),
          resolve = _.value.handle),
        Field("name", StringType,
          Some("The user-name of the user."),
          resolve = _.value.name),
        Field("subscriptions", ListType(UserType),
          Some("The users this user is subscribed to."),
          resolve = ctx ⇒ users.deferSeq(ctx.value.subscriptions.toSeq)),
        Field("subscribers", ListType(UserType),
          Some("The user that are subscribed to this user."),
          resolve = ctx ⇒ users.deferSeq(ctx.value.subscribers.toSeq))))

  lazy val UserCMType: ObjectType[Environment, User] =
    ObjectType("UserCommandModel", "The command model for User.",
      () ⇒ fields[Environment, User](
        Field("id", StringType,
          Some("The id of the user."),
          resolve = _.value.id),
        Field("handle", StringType,
          Some("The MyTwitter handle of the user."),
          resolve = _.value.handle),
        Field("name", StringType,
          Some("The user-name of the user."),
          resolve = _.value.name),
        Field("subscriptions", ListType(UserType),
          Some("The users this user is subscribed to."),
          resolve = ctx ⇒ users.deferSeq(ctx.value.subscriptions.toSeq))))

  lazy val UserTimelineType: ObjectType[Environment, UserTimeline] =
    ObjectType("UserTimeline", "The user-timeline of a user",
      () ⇒ fields[Environment, UserTimeline](
        Field("userId", UserType,
          Some("The user this timeline belongs to."),
          resolve = ctx ⇒ users.defer(ctx.value.userId)),
        Field("tweets", ListType(TweetLikeType),
          Some("A history of tweets and retweets a user made."),
          resolve = _.value.tweets),
        Field("likes", ListType(TweetType),
          Some("The twets a user liked."),
          resolve = ctx ⇒ tweets.deferSeq(ctx.value.likes))))

  lazy val HomeTimelineType: ObjectType[Environment, HomeTimeline] =
    ObjectType("UserTimeline", "The user-timeline of a user",
      () ⇒ fields[Environment, HomeTimeline](
        Field("userId", UserType,
          Some("The user this timeline belongs to."),
          resolve = ctx ⇒ users.defer(ctx.value.userId)),
        Field("tweets", ListType(TweetLikeType),
          Some("Tweets and retweets from users this user follows."),
          resolve = _.value.tweets)))

  lazy val TweetLikeType: ObjectType[Environment, TweetLike] =
    ObjectType("TweetLike", "A representation for tweets and retweets on timelines.",
      () ⇒ fields[Environment, TweetLike](
        Field("tweet", TweetType,
          Some("The tweet."),
          resolve = ctx ⇒ tweets.defer(ctx.value.tweetId)),
        Field("author", UserType,
          Some("The author of the tweet."),
          resolve = ctx ⇒ users.defer(ctx.value.authorId)),
        Field("user", OptionType(UserType),
          Some("The user who reposted the tweet.."),
          resolve = ctx ⇒ users.deferOpt(ctx.value.reposterId))))

  val TweetIdArg = Argument("tweetId", StringType, description = "Id of a tweet")
  val BodyArg = Argument("body", StringType, description = "The body of the tweet")
  val UserIdArg = Argument("userId", StringType, description = "Id of the subject user")
  val AuthorIdArg = Argument("authorId", StringType, description = "The id of the author.")
  val SubscriptionIdArg = Argument("subscriptionId", StringType, description = "Id of the user to subscribe to")
  val HandleArg = Argument("handle", StringType, description = "handle of the user")
  val NameArg = Argument("name", StringType, description = "name of the user")

  val QueryType = ObjectType(
    "Query", fields[Environment, Unit](
      Field("userTimeline", UserTimelineType,
        arguments = UserIdArg :: Nil,
        resolve = (ctx) ⇒ userTimelines.defer(ctx.arg(UserIdArg))),
      Field("homeTimeline", HomeTimelineType,
        arguments = UserIdArg :: Nil,
        resolve = (ctx) ⇒ homeTimelines.defer(ctx.arg(UserIdArg))),
      Field("user", UserType,
        arguments = UserIdArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.users.getById(ctx.arg(UserIdArg))),
      Field("tweet", TweetType,
        arguments = TweetIdArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.tweets.getById(ctx.arg(TweetIdArg)))))

  val MutationType = ObjectType(
    "Mutation", fields[Environment, Unit](

      // User Mutations
      Field("registerUser", UserCMType,
        arguments = HandleArg :: NameArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.userCommands.registerUser(ctx.arg(HandleArg), ctx.arg(NameArg))),
      Field("setUserName", UserCMType,
        arguments = UserIdArg :: NameArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.userCommands.setUserName(ctx.arg(UserIdArg), ctx.arg(NameArg))),
      Field("addSubscription", UserCMType,
        arguments = UserIdArg :: SubscriptionIdArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.userCommands.addSubscription(ctx.arg(UserIdArg), ctx.arg(SubscriptionIdArg))),
      Field("removeSubscription", UserCMType,
        arguments = UserIdArg :: SubscriptionIdArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.userCommands.removeSubscription(ctx.arg(UserIdArg), ctx.arg(SubscriptionIdArg))),
      Field("deleteUser", BooleanType,
        arguments = UserIdArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.userCommands.deleteUser(ctx.arg(UserIdArg))),


      Field("postTweet", TweetCMType,
        arguments = AuthorIdArg :: BodyArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.tweetCommands.postTweet(ctx.arg(AuthorIdArg), ctx.arg(BodyArg))),
      Field("repostTweet", TweetCMType,
        arguments = TweetIdArg:: UserIdArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.tweetCommands.repostTweet(ctx.arg(TweetIdArg), ctx.arg(UserIdArg))),
      Field("deletRepost", TweetCMType,
        arguments = TweetIdArg:: UserIdArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.tweetCommands.deletRepost(ctx.arg(TweetIdArg), ctx.arg(UserIdArg))),
      Field("deleteTweet", BooleanType,
        arguments = TweetIdArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.tweetCommands.deleteTweet(ctx.arg(TweetIdArg)))))

  val schema = Schema(QueryType, Some(MutationType))
}
