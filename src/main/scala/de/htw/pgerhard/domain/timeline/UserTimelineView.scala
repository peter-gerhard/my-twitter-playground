package de.htw.pgerhard.domain.timeline

import de.htw.pgerhard.domain.generic.View
import de.htw.pgerhard.domain.timeline.UserTimelineEvents._

class UserTimelineView(override val persistenceId: String, override val viewId: String)
  extends View[UserTimelineProjection] {

  override def receiveEvent: Receive = {
    case ev: UserTimelineCreatedEvent ⇒
      setState(Some(UserTimelineProjection.fromEvent(ev)))
    case ev: UserTweetedEvent ⇒
      alterState(tl ⇒ tl.copy(tweets = TweetRef(ev.tweetId) +: tl.tweets))
    case ev: UserDeletedTweetEvent ⇒
      alterState(tl ⇒ tl.copy(tweets = tl.tweets.filterNot(_.tweetId == ev.tweetId)))
    case ev: UserRetweetedEvent ⇒
      alterState(tl ⇒ tl.copy(tweets = Retweet(tl.userId, ev.tweetId) +: tl.tweets))
    case ev: UserDeletedRetweetEvent ⇒
      alterState(tl ⇒ tl.copy(tweets = tl.tweets.filterNot(_.tweetId == ev.tweetId)))
    case ev: UserTimelineDeletedEvent ⇒
      setState(None)
  }
}

object UserTimelineView {
  def apply(persistenceId: String, viewId: String) =
    new UserTimelineView(persistenceId, viewId)
}
