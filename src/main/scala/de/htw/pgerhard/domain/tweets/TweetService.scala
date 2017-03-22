package de.htw.pgerhard.domain.tweets

import akka.actor.ActorRef
import akka.util.Timeout
import de.htw.pgerhard.domain.generic.Connector
import de.htw.pgerhard.domain.tweets.TweetCommands._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class TweetService(
    val repo: ActorRef)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout,
    val ct: ClassTag[Tweet])
  extends Connector[Tweet] {

  def create(authorId: String, body: String): Future[Tweet] =
    sendMessage(CreateTweetCommand(authorId, body)).mapTo[Tweet]

  def addRetweeter(tweetId: String, userId: String): Future[Tweet] =
    sendMessageTo(tweetId, AddRetweeterCommand(userId)).mapTo[Tweet]

  def removeRetweeter(tweetId: String, userId: String): Future[Tweet] =
    sendMessageTo(tweetId, RemoveRetweeterCommand(userId)).mapTo[Tweet]

  def addLiker(tweetId: String, userId: String): Future[Tweet] =
    sendMessageTo(tweetId, AddLikerCommand(userId)).mapTo[Tweet]

  def removeLiker(tweetId: String, userId: String): Future[Tweet] =
    sendMessageTo(tweetId, RemoveLikerCommand(userId)).mapTo[Tweet]

  def delete(tweetId: String): Future[Boolean] =
    sendMessageTo(tweetId, DeleteTweetCommand).mapTo[Boolean]
}
