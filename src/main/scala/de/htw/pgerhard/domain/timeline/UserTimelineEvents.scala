package de.htw.pgerhard.domain.timeline

import de.htw.pgerhard.domain.generic.Event

object UserTimelineEvents {

  sealed trait UserTimelineEvent extends Event[UserTimeline]

  case class UserTimelineCreatedEvent(timelineId: String, userId: String) extends UserTimelineEvent

  case class UserTweetedEvent(timelineId: String, tweetId: String) extends UserTimelineEvent

  case class UserDeletedTweetEvent(timelineId: String, tweetId: String) extends UserTimelineEvent

  case class UserRetweetedEvent(timelineId: String, tweetId: String, authorId: String) extends UserTimelineEvent

  case class UserDeletedRetweetEvent(timelineId: String, tweetId: String) extends UserTimelineEvent

  case class UserTimelineDeletedEvent(timelineId: String) extends UserTimelineEvent
}