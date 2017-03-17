package de.htw.pgerhard.domain.timeline

import de.htw.pgerhard.domain.timeline.UserTimelineEvents.UserTimelineCreatedEvent
import scala.collection.immutable.Seq

case class UserTimelineProjection(id: String, userId: String, tweets: Seq[TweetRef])

case class TweetRef(id: String, isRetweet: Boolean = false)

object UserTimelineProjection {
  def fromEvent(ev: UserTimelineCreatedEvent) =
    UserTimelineProjection(ev.timelineId, ev.userId, Seq.empty)
}