package de.htw.pgerhard.domain.tweets

import akka.persistence.RecoveryFailure
import de.htw.pgerhard.domain.generic.AggregateRootProcessor
import de.htw.pgerhard.domain.tweets.TweetCommands._
import de.htw.pgerhard.domain.tweets.TweetEvents._

class TweetProcessor(val persistenceId: String) extends AggregateRootProcessor[Tweet] {

  override type CreatedEvent = TweetCreatedEvent

  override def aggregateRootFactory: (TweetCreatedEvent) => Tweet = Tweet.fromEvent

  override def receiveRecover: Receive = {
    case event: TweetCreatedEvent ⇒ handleCreation(event)
    case TweetDeletedEvent ⇒ handleDeletion()
    case event: TweetEvent ⇒ handleUpdate(event)
    case RecoveryFailure(e) =>
      println(s"recovery failed (error = ${e.getMessage})")
  }

  override def receiveBeforeInitialization: Receive = {
    case CreateTweetCommand(authorId, body) ⇒
      persist(TweetCreatedEvent(persistenceId, authorId, body, timestamp))
    case _: TweetCommand ⇒
      reportState()
  }

  override def receiveWhenInitialized: Receive = {
    case DeleteTweetCommand ⇒
      persist(TweetDeletedEvent(persistenceId)) { _ ⇒
        handleDeletion()
        reportState()
      }
  }

  private def timestamp = System.currentTimeMillis()
}

object TweetProcessor {
  def apply(persistenceId: String) = new TweetProcessor(persistenceId)
}
