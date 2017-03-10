package de.htw.pgerhard

import de.htw.pgerhard.domain.tweets.Tweet
import de.htw.pgerhard.domain.users.User
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{Field, _}

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
          resolve = u ⇒ u.ctx.users.getById(u.value.authorId)),
        Field("body", StringType,
          Some("The body of the tweet."),
          resolve = _.value.body),
        Field("timestamp", LongType,
          Some("The time when the tweet was tweeted."),
          resolve = _.value.timestamp),
        Field("likeCount", IntType,
          Some("The number of people who liked this tweet."),
          resolve = _.value.likedBy.size),
        Field("retweetCount", IntType,
          Some("The number of people who retweeted this tweet."),
          resolve = _.value.retweetedBy.size)))

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
        Field("follows", ListType(StringType),
          Some("The users which this user follows."),
          resolve = _.value.follows)))

  val TweetIdArg = Argument("tweetId", StringType, description = "Id of the tweet")
  val AuthorArg = Argument("authorId", StringType, description = "Id of the author")
  val UserArg = Argument("userId", StringType, description = "Id of the user")
  val BodyArg = Argument("body", StringType, description = "The body of the tweet")

  val UserIdArg = Argument("userId", StringType, description = "Id of the user")
  val FollowIdArg = Argument("followId", StringType, description = "Id of the user to follow")
  val HandleArg = Argument("handle", StringType, description = "handle of the user")
  val NameArg = Argument("name", StringType, description = "name of the user")

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
      // Tweet Mutations
      Field("tweet", OptionType(TweetType),
        arguments = AuthorArg :: BodyArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.tweets.create(ctx.arg(AuthorArg), ctx.arg(BodyArg))),
      Field("retweet", OptionType(TweetType),
        arguments =  TweetIdArg :: UserArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.tweets.retweet(ctx.arg(TweetIdArg), ctx.arg(UserArg))),
      Field("undoRetweet", OptionType(TweetType),
        arguments =  TweetIdArg :: UserArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.tweets.undoRetweet(ctx.arg(TweetIdArg), ctx.arg(UserArg))),
      Field("like", OptionType(TweetType),
        arguments =  TweetIdArg :: UserArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.tweets.like(ctx.arg(TweetIdArg), ctx.arg(UserArg))),
      Field("undoLike", OptionType(TweetType),
        arguments =  TweetIdArg :: UserArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.tweets.undoLike(ctx.arg(TweetIdArg), ctx.arg(UserArg))),
      Field("deleteTweet", OptionType(TweetType),
        arguments =  TweetIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.tweets.delete(ctx.arg(TweetIdArg))),

      // User Mutations
      Field("registerUser", OptionType(UserType),
        arguments = HandleArg :: NameArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.create(ctx.arg(HandleArg), ctx.arg(NameArg))),
      Field("setUserName", OptionType(UserType),
        arguments = UserIdArg :: NameArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.setName(ctx.arg(UserIdArg), ctx.arg(NameArg))),
      Field("followUser", OptionType(UserType),
        arguments = UserIdArg :: FollowIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.follow(ctx.arg(UserIdArg), ctx.arg(FollowIdArg))),
      Field("deleteUser", OptionType(UserType),
        arguments = UserIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.users.delete(ctx.arg(UserIdArg)))))

  val schema = Schema(QueryType, Some(MutationType))
}
