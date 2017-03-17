package de.htw.pgerhard.domain.timeline

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import de.htw.pgerhard.domain.timeline.UserTimelineCommands._
import de.htw.pgerhard.domain.tweets.{Tweet, TweetConnector}
import de.htw.pgerhard.domain.{Envelope, Get}
import de.htw.pgerhard.util.FutureOption

import scala.concurrent.{ExecutionContext, Future}

class UserTimelineConnector(
    userTimelineRepo: ActorRef,
    tweets: TweetConnector)(
  implicit
    ec: ExecutionContext,
    timeout: Timeout) {

  def getByUserId(userId: String): Future[Option[UserTimeline]] =
    sendMessage(Envelope(timelineId(userId), Get))

  def getMultipleByUserIds(ids: Seq[String]): Future[Seq[UserTimeline]] =
    Future.sequence(ids.map(getByUserId)).map(_.flatten)

  def createForUser(userId: String): Future[Option[UserTimeline]] =
    sendMessage(CreateUserTimelineCommand(timelineId(userId)))

  def postTweet(userId: String, body: String): Future[Option[Tweet]] =
    for {
      tweet ← FutureOption(tweets.create(userId, body))
      _     ← FutureOption(sendMessage(Envelope(timelineId(userId), PostTweetCommand(tweet.id))))
    } yield tweet

  def deleteTweet(userId: String, tweetId: String): Future[Option[Tweet]] =
    for {
      _     ← FutureOption(sendMessage(Envelope(timelineId(userId), DeleteTweetCommand(tweetId))))
      tweet ← FutureOption(tweets.delete(tweetId))
    } yield tweet

  // Todo return retweet? this does not report if an update happened
  def postRetweet(userId: String, tweetId: String): Future[Option[Boolean]] =
    for {
      tweet ← FutureOption(tweets.getById(tweetId))
      _     ← FutureOption(sendMessage(Envelope(timelineId(userId), PostRetweetCommand(tweetId, tweet.authorId))))
    } yield true

  def deleteRetweet(userId: String, tweetId: String): Future[Option[Boolean]] =
    for {
      _     ← FutureOption(sendMessage(Envelope(timelineId(userId), DeleteRetweetCommand(tweetId))))
      _     ← FutureOption(tweets.delete(tweetId))
    } yield true

  def deleteForUser(userId: String): Future[Option[UserTimeline]] =
    sendMessage(Envelope(timelineId(userId), DeleteUserTimelineCommand))

  private def sendMessage(message: Any) =
    (userTimelineRepo ? message).mapTo[Option[UserTimeline]]

  private def timelineId(userId: String) = s"timeline_$userId"

}