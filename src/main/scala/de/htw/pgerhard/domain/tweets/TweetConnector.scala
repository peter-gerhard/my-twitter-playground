package de.htw.pgerhard.domain.tweets

import akka.actor.ActorRef
import akka.util.Timeout
import de.htw.pgerhard.domain.generic.Connector
import de.htw.pgerhard.domain.tweets.TweetCommands._

import scala.concurrent.{ExecutionContext, Future}

class TweetConnector(
    val repo: ActorRef)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout)
  extends Connector[Tweet] {

  def create(authorId: String, body: String): Future[Tweet] =
    sendMessage(CreateTweetCommand(authorId, body))

  def addRetweeter(tweetId: String, userId: String): Future[Tweet] =
    sendMessageTo(tweetId, AddRetweeterCommand(userId))

  def removeRetweeter(tweetId: String, userId: String): Future[Tweet] =
    sendMessageTo(tweetId, RemoveRetweeterCommand(userId))

  def addLiker(tweetId: String, userId: String): Future[Tweet] =
    sendMessageTo(tweetId, AddLikerCommand(userId))

  def removeLiker(tweetId: String, userId: String): Future[Tweet] =
    sendMessageTo(tweetId, RemoveLikerCommand(userId))

  def delete(tweetId: String): Future[Boolean] =
    for {
      _ ← sendMessageTo[Unit](tweetId, DeleteTweetCommand)
    } yield true
}
