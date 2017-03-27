package de.htw.pgerhard.domain.tweets

import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import de.htw.pgerhard.domain.generic.{Envelope, Repository, RepositoryConnector}
import de.htw.pgerhard.domain.tweets.commands._

import scala.concurrent.{ExecutionContext, Future}

class TweetRepository(
    val repository: ActorRef)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout)
  extends RepositoryConnector {

  def postTweet(authorId: String, body: String): Future[Tweet] =
    askRepo(PostTweetCommand(authorId, body)).mapTo[Tweet]

  def repostTweet(tweetId: String, userId: String, authorId: String): Future[Tweet] =
    askRepo(tweetId, RepostTweetCommand(userId, authorId)).mapTo[Tweet]

  def deleteRepost(tweetId: String, userId: String): Future[Tweet] =
    askRepo(tweetId, DeleteRepostCommand(userId)).mapTo[Tweet]

  def deleteTweet(tweetId: String, authorId: String, repostedBy: Set[String]): Future[Boolean] =
    askRepo(tweetId, DeleteTweetCommand(authorId, repostedBy)).mapTo[Boolean]
}

class TweetRepositoryActor extends Repository {

  override protected def childProps(id: String): Props = TweetProcessor.props(id)

  override def receive: Receive = {
    case cmd: PostTweetCommand ⇒
      getChild(randomId) forward cmd

    case env: Envelope ⇒
      getChild(env.id) forward env.msg
  }
}

object TweetRepositoryActor {
  def props: Props = Props(new TweetRepositoryActor)
}