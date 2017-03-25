package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.generic.{AggregateRoot, AggregateRootFactory}
import de.htw.pgerhard.domain.tweets.events._

case class Tweet(
    id: String,
    authorId: String,
    timestamp: Long,
    body: String,
    likedBy: Set[String],
    repostedBy: Set[String])
  extends AggregateRoot[Tweet, TweetEvent] {

  def updated(event: TweetEvent): Tweet = event match {
    case ev: TweetRepostedEvent      ⇒ copy(repostedBy = repostedBy + ev.userId)
    case ev: TweetRepostDeletedEvent ⇒ copy(repostedBy = repostedBy - ev.userId)
    case _ ⇒ throw new IllegalArgumentException
  }
}

object Tweet extends AggregateRootFactory[Tweet, TweetEvent] {
  def fromCreatedEvent(event: TweetEvent): Tweet = event match {
    case ev: TweetPostedEvent ⇒
      Tweet(ev.tweetId, ev.authorId, ev.timestamp, ev.body, Set.empty, Set.empty)

    case _ ⇒
      throw new IllegalArgumentException
  }
}