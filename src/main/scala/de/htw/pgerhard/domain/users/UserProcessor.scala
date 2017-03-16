package de.htw.pgerhard.domain.users

import akka.persistence.RecoveryFailure
import de.htw.pgerhard.domain.generic.AggregateRootProcessor
import de.htw.pgerhard.domain.users.UserEvents._
import de.htw.pgerhard.domain.users.UserCommands._

class UserProcessor(override val persistenceId: String) extends AggregateRootProcessor[User] {

  override type CreatedEvent = UserRegisteredEvent

  override def aggregateRootFactory: UserRegisteredEvent => User = User.fromEvent

  override def receiveRecover: Receive = {
    case event: UserRegisteredEvent ⇒ handleCreation(event)
    case _: UserDeletedEvent ⇒ handleDeletion()
    case event: UserEvent ⇒ handleUpdate(event)
    case RecoveryFailure(e) =>
      println(s"recovery failed (error = ${e.getMessage})")
  }

  override def receiveBeforeInitialization: Receive = {
    case RegisterUserCommand(handle, name) ⇒
      persist(UserRegisteredEvent(persistenceId, handle, name)) { event ⇒
        handleCreation(event)
        reportState()
      }
    case _: UserCommand ⇒
      reportState()
  }

  override def receiveWhenInitialized: Receive = {
    case SetUserNameCommand(name) ⇒
      persist(UserNameSetEvent(persistenceId, name)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case FollowUserCommand(subscriptionId) ⇒
      state.filter(!_.following.contains(subscriptionId)) foreach { _ ⇒
        persist(UserFollowedEvent(persistenceId, subscriptionId)) { event ⇒
          handleUpdate(event)
          reportState()
        }
      }
    case UnfollowUserCommand(subscriptionId) ⇒
      state.filter(_.following.contains(subscriptionId)) foreach { _ ⇒
        persist(UserUnfollowedEvent(persistenceId, subscriptionId)) { event ⇒
          handleUpdate(event)
          reportState()
        }
      }
    case AddFollowerCommand(subscriberId) ⇒
      state.filter(!_.followers.contains(subscriberId)) foreach { _ ⇒
        persist(FollowerAddedEvent(persistenceId, subscriberId)) { event ⇒
          handleUpdate(event)
        }
      }
    case RemoveFollowerCommand(subscriberId) ⇒
      state.filter(_.followers.contains(subscriberId)) foreach { _ ⇒
        persist(FollowerRemovedEvent(persistenceId, subscriberId)) { event ⇒
          handleUpdate(event)
        }
      }
    case DeleteUserCommand ⇒
      persist(UserDeletedEvent(persistenceId)) { _ ⇒
        handleDeletion()
        reportState()
      }
  }
}

object UserProcessor {
  def apply(id: String) = new UserProcessor(id)
}
