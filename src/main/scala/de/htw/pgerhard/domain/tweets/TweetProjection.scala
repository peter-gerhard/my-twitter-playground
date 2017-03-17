package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.tweets.TweetEvents.TweetCreatedEvent

case class TweetProjection(
    id: String,
    authorId: String,
    timestamp: Long,
    body: String,
    likeCount: Int,
    retweetCount: Int)

object TweetProjection {
  def fromEvent(ev: TweetCreatedEvent) =
    TweetProjection(ev.id, ev.authorId, ev.timestamp, ev.body, 0, 0)
}
