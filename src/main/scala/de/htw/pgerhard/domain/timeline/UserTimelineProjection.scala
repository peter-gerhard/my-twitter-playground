package de.htw.pgerhard.domain.timeline

import de.htw.pgerhard.domain.generic.Event
import de.htw.pgerhard.domain.timeline.UserTimelineEvents._

import scala.collection.immutable.Seq

case class UserTimelineProjection(
    id: String,
    userId: String,
    tweets: Seq[WithTweet]) {

  def updated(event: Event[UserTimeline]): UserTimelineProjection = event match {
    case ev: UserTweetedEvent ⇒ copy(tweets = TweetRef(ev.tweetId) +: tweets)
    case ev: UserDeletedTweetEvent ⇒  copy(tweets = tweets.filterNot(_.tweetId == ev.tweetId))
    case ev: UserRetweetedEvent ⇒ copy(tweets = Retweet(userId, ev.tweetId) +: tweets)
    case ev: UserDeletedRetweetEvent ⇒ copy(tweets = tweets.filterNot(_.tweetId == ev.tweetId))
    case _ ⇒ throw new IllegalArgumentException
  }
}

object UserTimelineProjection {
  def fromEvent(ev: UserTimelineCreatedEvent) =
    UserTimelineProjection(ev.timelineId, ev.userId, Seq.empty)
}

sealed trait WithTweet { def tweetId: String }
case class TweetRef(tweetId: String) extends WithTweet
case class Retweet(userId: String, tweetId: String) extends WithTweet