package de.htw.pgerhard.domain.tweets

object TweetEvents {

  trait TweetEvent

  case class TweetPostedEvent(tweetId: String, authorId: String, body: String, timestamp: Long) extends TweetEvent

  case class TweetRetweetedEvent(tweetId: String, userId: String, timestamp: Long) extends TweetEvent

  case class RetweetTweetUndoneEvent(tweetId: String, userId: String) extends TweetEvent

  case class TweetLikedEvent(tweetId: String, userId: String) extends TweetEvent

  case class LikeTweetUndoneEvent(tweetId: String, userId: String) extends TweetEvent

  case class TweetDeletedEvent(tweetId: String) extends TweetEvent

}