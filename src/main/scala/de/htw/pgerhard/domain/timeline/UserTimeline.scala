package de.htw.pgerhard.domain.timeline

import de.htw.pgerhard.domain.generic.{AggregateRoot, Event}
import de.htw.pgerhard.domain.timeline.UserTimelineEvents._

case class UserTimeline(
    id: String,
    userId: String,
    retweets: Set[String])
 extends AggregateRoot[UserTimeline] {

  override def updated(event: Event[UserTimeline]): UserTimeline = event match {
    case _: UserTweetedEvent ⇒ this
    case _: UserDeletedTweetEvent ⇒  this
    case ev: UserRetweetedEvent ⇒ copy(retweets = retweets + ev.tweetId)
    case ev: UserDeletedRetweetEvent ⇒ copy(retweets = retweets - ev.tweetId)
    case _ ⇒ throw new IllegalArgumentException
  }
}

object UserTimeline {
  def fromEvent(ev: UserTimelineCreatedEvent): UserTimeline =
    UserTimeline(ev.timelineId, ev.userId, Set.empty)
}
