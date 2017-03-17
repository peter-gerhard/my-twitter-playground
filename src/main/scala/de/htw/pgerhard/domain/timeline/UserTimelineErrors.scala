package de.htw.pgerhard.domain.timeline

object UserTimelineErrors {

  sealed trait UserTimelineError

  case class AlreadyTweeted(timelineId: String, tweetId: String) extends UserTimelineError

  case class TweetNotFound(timelineId: String, tweetId: String) extends UserTimelineError

  case class AlreadyRetweeted(timelineId: String, tweetId: String) extends UserTimelineError

  case class RetweetNotFound(timelineId: String, tweetId: String) extends UserTimelineError
}
