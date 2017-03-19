package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.generic.Error

object TweetErrors {

  case class TweetNotFound(id: String) extends Error

  case class DuplicateRetweet(tweetId: String, userId: String) extends Error

  case class RetweeterNotFound(tweetId: String, userId: String) extends Error

  case class DuplicateLike(tweetId: String, userId: String) extends Error

  case class LikerNotFound(tweetId: String, userId: String) extends Error
}
