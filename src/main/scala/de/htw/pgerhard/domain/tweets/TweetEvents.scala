package de.htw.pgerhard.domain.tweets

object TweetEvents {

  trait TweetUpdatedEvent

  case class TweetCreatedEvent(tweetId: String, authorId: String, body: String, timestamp: Long)
  case class TweetRetweetedEvent(tweetId: String, userId: String) extends TweetUpdatedEvent
  case class RetweetTweetUndoneEvent(tweetId: String, userId: String) extends TweetUpdatedEvent
  case class TweetLikedEvent(tweetId: String, userId: String) extends TweetUpdatedEvent
  case class LikeTweetUndoneEvent(tweetId: String, userId: String) extends TweetUpdatedEvent
  case class TweetDeletedEvent(tweetId: String)
}
