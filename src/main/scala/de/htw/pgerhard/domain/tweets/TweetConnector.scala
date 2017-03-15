package de.htw.pgerhard.domain.tweets

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import de.htw.pgerhard.domain.Get
import de.htw.pgerhard.domain.tweets.TweetCommands._
import de.htw.pgerhard.domain.tweets.TweetRepository.Envelope

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class TweetConnector(val repo: ActorRef)(implicit ec: ExecutionContext) {

  private implicit val timeout = Timeout(5 seconds)

  def getById(id: String): Future[Option[Tweet]] =
    sendMessage(Envelope(id, Get))

  def getMultipleByIds(ids: Seq[String]): Future[Seq[Tweet]] =
    Future.sequence(ids.map(id ⇒ getById(id))).map(_.flatten)

  def create(authorId: String, body: String): Future[Option[Tweet]] =
    sendMessage(PostTweetCommand(authorId, body))

  def retweet(tweetId: String, userId: String): Future[Option[Tweet]] =
    sendMessage(Envelope(tweetId, AddRepostCommand(userId)))

  def undoRetweet(tweetId: String, userId: String): Future[Option[Tweet]] =
    sendMessage(Envelope(tweetId, RemoveRepostCommand(userId)))

  def like(tweetId: String, userId: String): Future[Option[Tweet]] =
    sendMessage(Envelope(tweetId, AddLikeCommand(userId)))

  def undoLike(tweetId: String, userId: String): Future[Option[Tweet]] =
    sendMessage(Envelope(tweetId, RemoveLikeCommand(userId)))

  def delete(tweetId: String): Future[Option[Tweet]] =
    sendMessage(Envelope(tweetId, DeleteTweetCommand))

  private def sendMessage(message: Any) =
    (repo ? message).mapTo[Option[Tweet]]
}
