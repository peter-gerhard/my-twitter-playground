package de.htw.pgerhard.domain.timeline

import de.htw.pgerhard.domain.timeline.UserTimelineEvents.UserTimelineCreatedEvent
import scala.collection.immutable.Seq

case class UserTimelineProjection(id: String, userId: String, tweets: Seq[WithTweet])

trait WithTweet {
  def tweetId: String
}

case class TweetRef(tweetId: String) extends WithTweet
case class Retweet(userId: String, tweetId: String) extends WithTweet

object UserTimelineProjection {
  def fromEvent(ev: UserTimelineCreatedEvent) =
    UserTimelineProjection(ev.timelineId, ev.userId, Seq.empty)
}