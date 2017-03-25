package de.htw.pgerhard.domain.tweets.errors

import de.htw.pgerhard.domain.generic.{MyTwitterError, UserFacingError}

sealed trait TweetError extends MyTwitterError

case class TweetNotFound(tweetId: String) extends TweetError with UserFacingError {
  override def getMessage(): String = s"Tweet '$tweetId' not found."
}

case class TweetAlreadyExists(tweetId: String) extends TweetError with UserFacingError {
  override def getMessage(): String = s"Tweet '$tweetId' already exists."
}

case class DuplicateRepost(tweetId: String, userId: String) extends TweetError with UserFacingError {
  override def getMessage(): String = s"Tweet '$tweetId' was already reposted by user '$userId'."
}

case class RepostNotFound(tweetId: String, userId: String) extends TweetError with UserFacingError {
  override def getMessage(): String = s"Tweet '$tweetId' was not reposted by user '$userId'."
}
