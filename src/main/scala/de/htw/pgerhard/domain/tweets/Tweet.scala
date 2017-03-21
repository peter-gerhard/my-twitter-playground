package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.generic.{AggregateRoot, Event}
import de.htw.pgerhard.domain.tweets.TweetEvents._

case class Tweet(
    id: String,
    authorId: String,
    timestamp: Long,
    body: String,
    likers: Set[String],
    retweeters: Set[String])
  extends AggregateRoot[Tweet] {

  def updated(event: Event[Tweet]): Tweet = event match {
    case RetweeterAddedEvent(tweetId, userId) ⇒ copy(retweeters = retweeters + userId)
    case RetweeterRemovedEvent(tweetId, userId) ⇒ copy(retweeters = retweeters - userId)
//    case LikerAddedEvent(tweetId, userId) ⇒ copy(likers = likers + userId)
//    case LikerRemovedEvent(tweetId, userId) ⇒ copy(likers = likers - userId)
    case _ ⇒ throw new IllegalArgumentException
  }
}

object Tweet {
  def fromEvent(ev: TweetCreatedEvent) =
    Tweet(ev.id, ev.authorId, ev.timestamp, ev.body, Set.empty, Set.empty)
}