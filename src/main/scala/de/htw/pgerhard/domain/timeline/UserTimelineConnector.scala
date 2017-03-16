package de.htw.pgerhard.domain.timeline

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import de.htw.pgerhard.domain.timeline.UserTimelineCommands._
import de.htw.pgerhard.domain.{Envelope, Get}

import scala.concurrent.{ExecutionContext, Future}

class UserTimelineConnector(repo: ActorRef)(implicit ec: ExecutionContext, timeout: Timeout) {

  def getById(id: String): Future[Option[UserTimeline]] =
    sendMessage(Envelope(id, Get))

  def getMultipleByIds(ids: Seq[String]): Future[Seq[UserTimeline]] =
    Future.sequence(ids.map(getById)).map(_.flatten)

  def create(userId: String): Future[Option[UserTimeline]] =
    sendMessage(CreateUserTimelineCommand(userId))

  def postTweet(id: String, tweetId: String): Future[Option[UserTimeline]] =
    sendMessage(Envelope(id, PostTweetCommand(tweetId)))

  def deleteTweet(id: String, tweetId: String): Future[Option[UserTimeline]] =
    sendMessage(Envelope(id, DeleteTweetCommand(tweetId)))

  def postRetweet(id: String, tweetId: String, authorId: String): Future[Option[UserTimeline]] =
    sendMessage(Envelope(id, PostRetweetCommand(tweetId, authorId)))

  def deleteRetweet(id: String, tweetId: String): Future[Option[UserTimeline]] =
    sendMessage(Envelope(id, DeleteRetweetCommand(tweetId)))

  def delete(id: String, tweetId: String): Future[Option[UserTimeline]] =
    sendMessage(Envelope(id, DeleteUserTimeLineCommand))

  private def sendMessage(message: Any) =
    (repo ? message).mapTo[Option[UserTimeline]]
}