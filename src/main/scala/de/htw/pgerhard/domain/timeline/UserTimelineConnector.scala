package de.htw.pgerhard.domain.timeline

import akka.actor.ActorRef
import akka.util.Timeout
import de.htw.pgerhard.domain.generic.Connector
import de.htw.pgerhard.domain.timeline.UserTimelineCommands._
import de.htw.pgerhard.domain.tweets.{Tweet, TweetConnector}

import scala.concurrent.{ExecutionContext, Future}

class UserTimelineConnector(
    val repo: ActorRef,
    val tweets: TweetConnector)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout)
  extends Connector[UserTimelineProjection] {

  def createForUser(userId: String): Future[UserTimeline] =
    sendMessage(CreateUserTimelineCommand(timelineId(userId)))

  def postTweet(userId: String, body: String): Future[Tweet] =
    for {
      tweet ← tweets.create(userId, body)
      _     ← sendMessageTo[UserTimeline](timelineId(userId), PostTweetCommand(tweet.id))
    } yield tweet

  def deleteTweet(userId: String, tweetId: String): Future[Boolean] =
    for {
      _ ← sendMessageTo[UserTimeline](timelineId(userId), DeleteTweetCommand(tweetId))
      _ ← tweets.delete(tweetId)
    } yield true

  def postRetweet(userId: String, tweetId: String): Future[Retweet] =
    for {
      tweet ← tweets.getById(tweetId)
      _     ← sendMessageTo[UserTimeline](timelineId(userId), PostRetweetCommand(tweetId, tweet.authorId))
    } yield Retweet(userId, tweet.id)

  def deleteRetweet(userId: String, tweetId: String): Future[Boolean] =
    for {
      _ ← sendMessageTo[UserTimeline](timelineId(userId), DeleteRetweetCommand(tweetId))
      _ ← tweets.delete(tweetId)
    } yield true

  def deleteForUser(userId: String): Future[Boolean] =
    for {
      timeline ← getById(timelineId(userId)) // Dont want projection here not consistent add Write Site Get
      _        ← Future.sequence(timeline.tweets collect { case t: TweetRef ⇒ tweets.delete(t.tweetId) })
      _        ← sendMessageTo[Unit](timelineId(userId), DeleteUserTimelineCommand)
    } yield true


  private def timelineId(userId: String) = s"timeline_$userId"
}