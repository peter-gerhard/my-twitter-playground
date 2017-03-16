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
    case cmd: RegisterUserCommand ⇒
      persist(UserRegisteredEvent(persistenceId, cmd.handle, cmd.name)) { event ⇒
        handleCreation(event)
        reportState()
      }
    case _: UserCommand ⇒
      reportState()
  }

  override def receiveWhenInitialized: Receive = {
    case cmd: SetUserNameCommand ⇒
      persist(UserNameSetEvent(persistenceId, cmd.name)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case cmd: FollowUserCommand ⇒
      state.filter(!_.following.contains(cmd.userId)) foreach { _ ⇒
        persist(UserFollowedEvent(persistenceId, cmd.userId)) { event ⇒
          handleUpdate(event)
          reportState()
        }
      }
    case cmd: UnfollowUserCommand ⇒
      state.filter(_.following.contains(cmd.userId)) foreach { _ ⇒
        persist(UserUnfollowedEvent(persistenceId, cmd.userId)) { event ⇒
          handleUpdate(event)
          reportState()
        }
      }
    case cmd: AddFollowerCommand ⇒
      state.filter(!_.followers.contains(cmd.userId)) foreach { _ ⇒
        persist(FollowerAddedEvent(persistenceId, cmd.userId)) { event ⇒
          handleUpdate(event)
        }
      }
    case cmd: RemoveFollowerCommand ⇒
      state.filter(_.followers.contains(cmd.userId)) foreach { _ ⇒
        persist(FollowerRemovedEvent(persistenceId, cmd.userId)) { event ⇒
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
