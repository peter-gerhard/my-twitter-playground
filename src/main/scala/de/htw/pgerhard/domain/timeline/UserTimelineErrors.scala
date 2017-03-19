package de.htw.pgerhard.domain.timeline

import de.htw.pgerhard.domain.generic.{Error, UserFacingError}

object UserTimelineErrors {

  case class UserTimelineNotFound(id: String) extends Error

  case class AlreadyTweeted(timelineId: String, tweetId: String) extends Error

  case class TweetNotFound(timelineId: String, tweetId: String) extends UserFacingError {
    override def getMessage(): String = s"Tweet '$tweetId' on timeline '$timelineId' not found."
  }

  case class AlreadyRetweeted(timelineId: String, tweetId: String) extends UserFacingError {
    override def getMessage(): String = s"Tweet '$tweetId' already tweeted on timeline '$timelineId'."
  }

  case class RetweetNotFound(timelineId: String, tweetId: String) extends UserFacingError {
    override def getMessage(): String = s"Retweet of tweet '$tweetId' on timeline '$timelineId' not found."
  }
}
