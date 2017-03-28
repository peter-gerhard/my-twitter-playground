package de.htw.pgerhard.domain.timelines

import akka.actor.Status.Failure
import akka.actor.ActorRef
import akka.util.Timeout
import de.htw.pgerhard.domain.generic._
import de.htw.pgerhard.domain.tweets.events._
import de.htw.pgerhard.domain.users.errors.UserNotFound
import de.htw.pgerhard.domain.users.events.UserDeletedEvent

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

class UserTimelineView(
    val view: ActorRef)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout,
    val classTag: ClassTag[UserTimeline])
  extends ViewConnector[UserTimeline]

class UserTimelineViewActor extends View {

  private val timelines = mutable.Map[String, UserTimeline]()

  override protected def handleEvent: EventHandler = {
    case ev: TweetPostedEvent ⇒
      updateTimeline(ev.authorId)(_.withAdditionalTweet(TweetLike(ev.tweetId, ev.authorId)))

    case ev: TweetRepostedEvent ⇒
      updateTimeline(ev.userId)(_.withAdditionalTweet(TweetLike(ev.tweetId, ev.authorId, Some(ev.userId))))

    case ev: TweetRepostDeletedEvent ⇒
      updateTimeline(ev.userId)(_.withRemovedTweet(ev.tweetId))

    case ev: TweetDeletedEvent ⇒
      updateTimeline(ev.authorId)(_.withRemovedTweet(ev.tweetId)) // Todo delete all retweets

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

  private def updateTimeline(userId: String)(fn: UserTimeline ⇒ UserTimeline) =
    timelines(userId) = fn(timelines.getOrElse(userId, UserTimeline(userId)))
}

object UserTimelineViewActor {
  def apply() = new UserTimelineViewActor
}

case class UserTimeline(
    userId: String,
    tweets: Seq[TweetLike],
    likes: Seq[String]) {

  def withAdditionalTweet(tweetLike: TweetLike): UserTimeline =
    copy(tweets = tweetLike +: tweets)

  def withRemovedTweet(tweetId: String): UserTimeline =
    copy(tweets = tweets.filter(_.tweetId != tweetId))
}

object UserTimeline {
  def apply(userId: String): UserTimeline = UserTimeline(userId, Seq.empty, Seq.empty)
}
