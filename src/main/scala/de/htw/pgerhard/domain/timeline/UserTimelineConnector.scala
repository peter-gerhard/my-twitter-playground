package de.htw.pgerhard.domain.timeline

import akka.actor.ActorRef
import akka.util.Timeout
import de.htw.pgerhard.domain.generic.Connector
import de.htw.pgerhard.domain.timeline.UserTimelineCommands._
import de.htw.pgerhard.domain.timeline.UserTimelineErrors.TweetNotFound
import de.htw.pgerhard.domain.tweets.TweetErrors.RetweeterNotFound
import de.htw.pgerhard.domain.tweets.{Tweet, TweetConnector}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class UserTimelineConnector(
    val repo: ActorRef,
    val tweets: TweetConnector)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout,
    val ct: ClassTag[UserTimeline])
  extends Connector[UserTimeline] {

  def getForUser(userId: String): Future[UserTimelineProjection] =
    sendMessageTo(timelineId(userId), GetView).mapTo[UserTimelineProjection]

  def createForUser(userId: String): Future[UserTimeline] =
    sendMessageTo(timelineId(userId), CreateUserTimelineCommand(userId)).mapTo[UserTimeline]

  def postTweet(userId: String, body: String): Future[Tweet] =
    for {
      tweet ← tweets.create(userId, body)
      _     ← sendMessageTo(timelineId(userId), PostTweetCommand(tweet.id)).mapTo[UserTimeline]
    } yield tweet

  def deleteTweet(userId: String, tweetId: String): Future[Boolean] =
    for {
      _   ← sendMessageTo(timelineId(userId), DeleteTweetCommand(tweetId)).mapTo[UserTimeline]
      res ← tweets.delete(tweetId)
    } yield res

  def postRetweet(userId: String, tweetId: String): Future[Retweet] =
    for {
      tweet ← tweets.getById(tweetId)
      _     ← sendMessageTo(timelineId(userId), PostRetweetCommand(tweetId, tweet.authorId)).mapTo[UserTimeline]
      _     ← tweets.addRetweeter(tweetId, userId)
    } yield Retweet(userId, tweet.id)

  def deleteRetweet(userId: String, tweetId: String): Future[Boolean] =
    for {
      _ ← sendMessageTo(timelineId(userId), DeleteRetweetCommand(tweetId)).mapTo[UserTimeline]
      res ← tweets.removeRetweeter(tweetId, userId).map(_ ⇒ true)
    } yield res

  def deleteForUser(userId: String): Future[Boolean] =
    for {
      timeline ← getById(timelineId(userId))
      _        ← deleteTweets(timeline.tweets)
      _        ← undoRetweets(timeline.retweets, timeline.userId)
      res      ← sendMessageTo(timelineId(userId), DeleteUserTimelineCommand).mapTo[Boolean]
    } yield res

  private def deleteTweets(tweetIds: Set[String]) =
    Future.sequence(
      tweetIds map { t ⇒
        tweets.delete(t)
          .recover {
            case e: TweetNotFound ⇒ false
          }})

  private def undoRetweets(tweetIds: Set[String], userId: String) =
    Future.sequence(
      tweetIds map { t ⇒
        tweets.removeRetweeter(t, userId)
          .map(_ ⇒ true)
          .recover {
            case e: RetweeterNotFound ⇒ false
          }})

  private def timelineId(userId: String) = s"timeline-$userId"
}