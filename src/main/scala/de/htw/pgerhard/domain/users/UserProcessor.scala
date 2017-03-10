package de.htw.pgerhard.domain.users

import akka.actor.ActorLogging
import akka.actor.Status.Failure
import akka.persistence._
import de.htw.pgerhard.Get
import de.htw.pgerhard.domain.users.UserCommands._
import de.htw.pgerhard.domain.users.UserEvents._

class UserProcessor(val persistenceId: String) extends PersistentActor with ActorLogging {
  var state: Option[User] = None

  override def receiveCommand: Receive = nonExistent orElse handlePersistenceMessages

  private def nonExistent: Receive = {
    case CreateUserCommand(handle, name) ⇒
      persist(UserCreatedEvent(persistenceId, handle, name)) { event ⇒
        handleCreation(event)
        reportState()
      }
    case _: UserCommand ⇒
      reportState()

    case Get ⇒
      reportState()
  }

  private def existent: Receive = {
    case SetUserNameCommand(x) ⇒
      persist(UserNameSetEvent(persistenceId, x)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case FollowUserCommand(x) ⇒
      persist(UserFollowedEvent(persistenceId, x)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case DeleteUserCommand ⇒
      persist(UserDeletedEvent(persistenceId)) { _ ⇒
        handleDeletion()
        reportState()
      }

    case Get ⇒
      reportState()
  }

  override def receiveRecover: Receive = {
    case event: UserCreatedEvent ⇒ handleCreation(event)
    case event: UserUpdatedEvent ⇒ handleUpdate(event)
    case UserDeletedEvent ⇒ handleDeletion()

    case SnapshotOffer(md, snapshot) ⇒
      restoreFromSnapshot(snapshot.asInstanceOf[Option[User]])
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

  private def handleCreation(event: UserCreatedEvent): Unit = {
    state = Some(User.fromEvent(event))
    context.become(existent orElse handlePersistenceMessages)
  }

  private def handleUpdate(event: UserUpdatedEvent): Unit = {
    state = state.map(_.updated(event))
  }

  private def handleDeletion(): Unit = {
    state = None
    context.become(nonExistent orElse handlePersistenceMessages)
  }

  private def restoreFromSnapshot(snapshot: Option[User]): Unit = {
    state = snapshot
    state.foreach(_ ⇒ context.become(existent orElse handlePersistenceMessages))
  }

  private def reportState() =
    sender() ! state

  override def preRestart(reason: Throwable, message: Option[Any]) {
    sender() ! Failure(reason)
    super.preRestart(reason, message)
  }
}

object UserProcessor {
  def apply(persistenceId: String) = new UserProcessor(persistenceId)
}