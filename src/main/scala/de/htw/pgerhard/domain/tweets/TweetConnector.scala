package de.htw.pgerhard.domain.tweets

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import de.htw.pgerhard.domain.{Envelope, Get}
import de.htw.pgerhard.domain.tweets.TweetCommands._

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
    sendMessage(CreateTweetCommand(authorId, body))

  def delete(tweetId: String): Future[Option[Tweet]] =
    sendMessage(Envelope(tweetId, DeleteTweetCommand))

  private def sendMessage(message: Any) =
    (repo ? message).mapTo[Option[Tweet]]
}
