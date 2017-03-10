package de.htw.pgerhard.domain.tweets

import akka.actor.ActorLogging
import akka.actor.Status.Failure
import akka.persistence._
import de.htw.pgerhard.Get
import de.htw.pgerhard.domain.tweets.TweetCommands._
import de.htw.pgerhard.domain.tweets.TweetEvents._

class TweetProcessor(val persistenceId: String) extends PersistentActor with ActorLogging  {

  var state: Option[Tweet] = None

  override def receiveCommand: Receive = nonExistent orElse handlePersistenceMessages

  private def nonExistent: Receive = {
    case CreateTweetCommand(authorId, body) ⇒
      persist(TweetCreatedEvent(persistenceId, authorId, body, System.currentTimeMillis())) { event ⇒
        handleCreation(event)
        reportState()
      }
    case _: TweetCommand ⇒
      reportState()

    case Get ⇒
      reportState()
  }

  private def existent: Receive = {
    case RetweetTweetCommand(userId) ⇒
      persist(TweetRetweetedEvent(persistenceId, userId)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case UndoRetweetTweetCommand(userId) ⇒
      persist(RetweetTweetUndoneEvent(persistenceId, userId)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case LikeTweetCommand(userId) ⇒
      persist(TweetLikedEvent(persistenceId, userId)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case UndoLikeTweetCommand(userId) ⇒
      persist(LikeTweetUndoneEvent(persistenceId, userId)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case DeleteTweetCommand ⇒
      persist(TweetDeletedEvent(persistenceId)) { _ ⇒
        handleDeletion()
        reportState()
      }
    case Get ⇒
      reportState()
  }

  override def receiveRecover: Receive = {
    case event: TweetCreatedEvent ⇒ handleCreation(event)
    case event: TweetUpdatedEvent ⇒ handleUpdate(event)
    case TweetDeletedEvent ⇒ handleDeletion()

    case SnapshotOffer(md, snapshot) ⇒
      restoreFromSnapshot(snapshot.asInstanceOf[Option[Tweet]])
      log.debug(s"state initialized: $state (metadata = $md)")
    case RecoveryFailure(e) =>
      log.debug(s"recovery failed (error = ${e.getMessage})")
  }

  def handlePersistenceMessages: Receive = {
    case SaveSnapshotSuccess(md) =>
      log.debug(s"snapshot saved (metadata = $md)")
    case SaveSnapshotFailure(md, e) =>
      log.debug(s"snapshot saving failed (metadata = $md, error = ${e.getMessage})")
    case PersistenceFailure(payload, snr, e) =>
      log.debug(s"persistence failed (payload = $payload, sequenceNr = $snr, error = ${e.getMessage})")
  }

  private def handleCreation(event: TweetCreatedEvent): Unit = {
    state = Some(Tweet.fromEvent(event))
    context.become(existent orElse handlePersistenceMessages)
  }

  private def handleUpdate(event: TweetUpdatedEvent): Unit = {
    state = state.map(_.updated(event))
  }

  private def handleDeletion(): Unit = {
    state = None
    context.become(nonExistent orElse handlePersistenceMessages)
  }

  private def restoreFromSnapshot(snapshot: Option[Tweet]): Unit = {
    state = snapshot
    state.foreach(_ ⇒ context.become(existent))
  }

  private def reportState() =
    sender() ! state

  override def preRestart(reason: Throwable, message: Option[Any]) {
    sender() ! Failure(reason)
    super.preRestart(reason, message)
  }
}

object TweetProcessor {
  def apply(persistenceId: String) = new TweetProcessor(persistenceId)
}
