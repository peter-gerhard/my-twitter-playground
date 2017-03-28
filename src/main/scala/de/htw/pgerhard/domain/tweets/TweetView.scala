package de.htw.pgerhard.domain.tweets

import akka.actor.Status.Failure
import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import de.htw.pgerhard.domain.generic._
import de.htw.pgerhard.domain.tweets.errors.TweetNotFound
import de.htw.pgerhard.domain.tweets.events._

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

class TweetView(
    val view: ActorRef)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout,
    val classTag: ClassTag[SimpleTweet])
  extends ViewConnector[SimpleTweet]

class TweetViewActor extends View {

  private val tweets = mutable.Map[String, SimpleTweet]()

  override protected def handleEvent: EventHandler = {
    case ev: TweetPostedEvent ⇒
      tweets(ev.tweetId) = SimpleTweet.fromCreatedEvent(ev)

    case ev: TweetRepostedEvent ⇒
      val tweet = tweets(ev.tweetId)
      tweets(ev.tweetId) = tweet.copy(repostCount = tweet.repostCount + 1)

    case ev: TweetRepostDeletedEvent ⇒
      val tweet = tweets(ev.tweetId)
      tweets(ev.tweetId) = tweet.copy(repostCount = tweet.repostCount - 1)

    case ev: TweetDeletedEvent ⇒
      tweets.remove(ev.tweetId)
  }

  override protected def receiveClientMessage: Receive = {
    case msg: GetById ⇒
      sender() ! tweets.getOrElse(msg.id, Failure(TweetNotFound(msg.id)))

    case msg: GetOptById ⇒
      sender() ! tweets.get(msg.id)

    case msg: GetSeqByIds ⇒
      sender() ! msg.ids.flatMap(tweets.get)
  }
}

object TweetViewActor {
  def props: Props = Props(new TweetViewActor)
}

case class SimpleTweet(
    id: String,
    authorId: String,
    body: String,
    timestamp: Long,
    likeCount: Int,
    repostCount: Int)

object SimpleTweet {
  def fromCreatedEvent(ev: TweetPostedEvent): SimpleTweet =
    SimpleTweet(ev.tweetId, ev.authorId, ev.body, ev.timestamp, 0, 0)
}
