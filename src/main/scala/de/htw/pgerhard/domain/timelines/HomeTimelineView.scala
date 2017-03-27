package de.htw.pgerhard.domain.timelines

import akka.actor.Status.Failure
import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import de.htw.pgerhard.domain.generic._
import de.htw.pgerhard.domain.tweets.events._
import de.htw.pgerhard.domain.users.errors.UserNotFound
import de.htw.pgerhard.domain.users.events.{UserDeletedEvent, UserSubscriptionAddedEvent, UserSubscriptionRemovedEvent}

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

class HomeTimelineView(
    val view: ActorRef)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout,
    val classTag: ClassTag[HomeTimeline])
  extends ViewConnector[HomeTimeline]

class HomeTimelineViewActor extends View {

  private val userSubscribers = mutable.Map[String, UserSubscribers]()
  private val timelines = mutable.Map[String, HomeTimeline]()

  override protected def handleEvent: EventHandler = {
    case ev: UserSubscriptionAddedEvent ⇒
      updateSubscribers(ev.subscriptionId, _.withAdditionalSubscriber(ev.userId))

    case ev: UserSubscriptionRemovedEvent ⇒
      updateSubscribers(ev.subscriptionId, _.withRemovedSubscriber(ev.userId))

    case ev: TweetPostedEvent ⇒
      val tweet = TweetLike(ev.tweetId, ev.authorId)
      forAllSubscribers(ev.authorId) { subscriberId ⇒
        updateTimeline(subscriberId, _.withAdditionalTweet(tweet))
      }

    case ev: TweetRepostedEvent ⇒
      val tweet = TweetLike(ev.tweetId, ev.authorId, Some(ev.userId))
      forAllSubscribers(ev.userId) { subscriberId ⇒
        updateTimeline(subscriberId, _.withAdditionalTweet(tweet))
      }

    case ev: TweetRepostDeletedEvent ⇒
      forAllSubscribers(ev.userId) { subscriberId ⇒
        updateTimeline(subscriberId, _.withRemovedTweet(ev.tweetId))
      }

    case ev: TweetDeletedEvent ⇒
      forAllSubscribers(ev.authorId) { subscriberId ⇒
        updateTimeline(subscriberId, _.withRemovedTweet(ev.tweetId))
      }

    case ev: UserDeletedEvent ⇒
      timelines.remove(ev.userId)

    case _ ⇒
  }

  override protected def receiveClientMessage: Receive = {
    case msg: GetById ⇒
      sender() ! timelines.getOrElse(msg.id, Failure(UserNotFound(msg.id)))

    case msg: GetOptById ⇒
      sender() ! timelines.get(msg.id)

    case msg: GetSeqByIds ⇒
      sender() ! msg.ids.flatMap(timelines.get)
  }

  private def forAllSubscribers(userId: String)(fn: String ⇒ Unit) = {
    val subscribers = userSubscribers(userId).subscribers
    subscribers.foreach(fn)
  }

  private def updateTimeline(userId: String, fn: HomeTimeline ⇒ HomeTimeline) =
    timelines(userId) = fn(timelines.getOrElse(userId, newTimeline(userId)))

  private def updateSubscribers(userId: String, fn: UserSubscribers ⇒ UserSubscribers) =
    userSubscribers(userId) = fn(userSubscribers.getOrElse(userId, newUserSubscribers))

  private def newTimeline(id: String) =
    HomeTimeline(id, Seq.empty)

  private def newUserSubscribers =
    UserSubscribers(Set.empty)
}

object HomeTimelineViewActor {
  def props: Props = Props(new HomeTimelineViewActor)
}

case class HomeTimeline(
    userId: String,
    tweets: Seq[TweetLike]) {

  def withAdditionalTweet(tweetLike: TweetLike): HomeTimeline =
    copy(tweets = tweetLike +: tweets)

  def withRemovedTweet(tweetId: String): HomeTimeline =
    copy(tweets = tweets.filter(_.tweetId != tweetId))
}

case class UserSubscribers(subscribers: Set[String]) {

  def withAdditionalSubscriber(subscriberId: String): UserSubscribers =
    copy(subscribers = subscribers + subscriberId)

  def withRemovedSubscriber(subscriberId: String): UserSubscribers =
    copy(subscribers = subscribers - subscriberId)
}
