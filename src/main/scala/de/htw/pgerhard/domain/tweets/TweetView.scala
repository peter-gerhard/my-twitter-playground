package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.generic.View
import de.htw.pgerhard.domain.tweets.TweetEvents._

class TweetView(override val persistenceId: String, override val viewId: String)
  extends View[TweetProjection]{

  override def receiveEvent: Receive = {
    case ev: TweetCreatedEvent ⇒
      setState(Some(TweetProjection.fromEvent(ev)))
//    case ev: RetweeterAddedEvent ⇒
//    case ev: RetweeterRemovedEvent ⇒
//    case ev: LikerAddedEvent ⇒
//    case ev: LikerRemovedEvent ⇒
    case ev: TweetDeletedEvent ⇒
      setState(None)
  }
}

object TweetView {
  def apply(persistenceId: String, viewId: String) =
    new TweetView(persistenceId, viewId)
}
