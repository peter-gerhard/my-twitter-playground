package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.tweets.TweetEvents._

case class Tweet(
    id: String,
    authorId: String,
    timestamp: Long,
    body: String,
    likedBy: Set[String],
    retweetedBy: Set[String]) {

  def updated(event: TweetUpdatedEvent): Tweet = event match {
    case TweetRetweetedEvent(tweetId, userId) ⇒ copy(retweetedBy = retweetedBy + userId)
    case RetweetTweetUndoneEvent(tweetId, userId) ⇒ copy(retweetedBy = retweetedBy - userId)
    case TweetLikedEvent(tweetId, userId) ⇒ copy(likedBy = likedBy + userId)
    case LikeTweetUndoneEvent(tweetId, userId) ⇒ copy(likedBy = likedBy - userId)
  }
}

object Tweet {
  def fromEvent(event: TweetCreatedEvent) =
    Tweet(event.tweetId, event.authorId, event.timestamp, event.body, Set.empty, Set.empty)
}