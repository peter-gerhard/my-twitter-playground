package de.htw.pgerhard.domain.tweets

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import de.htw.pgerhard.domain.{Envelope, Get}
import de.htw.pgerhard.domain.tweets.TweetCommands._

import scala.concurrent.{ExecutionContext, Future}

class TweetConnector(val repo: ActorRef)(implicit ec: ExecutionContext, timeout: Timeout) {

  def getById(id: String): Future[Option[TweetProjection]] =
    (repo ? Envelope(id, Get)).mapTo[Option[TweetProjection]]

  // Todo flatten is not allowed here since seq must have same lenght as ids
  def getMultipleByIds(ids: Seq[String]): Future[Seq[TweetProjection]] =
    Future.sequence(ids.map(id ⇒ getById(id))).map(_.flatten)

  // Todo correct return type
  def create(authorId: String, body: String): Future[Option[Tweet]] =
    sendMessage(CreateTweetCommand(authorId, body))

  def delete(tweetId: String): Future[Option[Tweet]] =
    sendMessage(Envelope(tweetId, DeleteTweetCommand))

  private def sendMessage(message: Any) =
    (repo ? message).mapTo[Option[Tweet]]
}
