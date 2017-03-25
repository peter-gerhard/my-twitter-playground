package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.generic.{AggregateRootFactory, AggregateRootProcessor}
import de.htw.pgerhard.domain.users.commands._
import de.htw.pgerhard.domain.users.errors._
import de.htw.pgerhard.domain.users.events._

class UserProcessor(val persistenceId: String) extends AggregateRootProcessor[User, UserEvent] {

  override def factory: AggregateRootFactory[User, UserEvent] = User

  override def receiveRecover: Receive = {
    case ev: UserRegisteredEvent  ⇒ handleCreation(ev)
    case ev: UserDeletedEvent     ⇒ handleDeletion()
    case ev: UserEvent            ⇒ handleUpdate(ev)
  }

  override def uninitialized: Receive = {
    case cmd: RegisterUserCommand ⇒
      persist(UserRegisteredEvent(persistenceId, cmd.handle, cmd.name)) { ev ⇒
        handleCreation(ev)
        reportState()
      }

    case _ ⇒
      reportFailure(notFound)
  }

  override def initialized: Receive = {
    case cmd: RegisterUserCommand ⇒
      reportFailure(UserAlreadyExists(persistenceId))

    case cmd: SetUserNameCommand ⇒
      persistUpdate(UserNameSetEvent(persistenceId, cmd.name))

    case cmd: AddSubscriptionCommand ⇒
      persistUpdateIf(isNotFollowing(cmd.subscriptionId))(
        UserSubscriptionAddedEvent(persistenceId, cmd.subscriptionId),
        UserAlreadySubscribed(persistenceId, cmd.subscriptionId))

    case cmd: RemoveSubscriptionCommand ⇒
      persistUpdateIf(isFollowing(cmd.subscriptionId))(
        UserSubscriptionRemovedEvent(persistenceId, cmd.subscriptionId),
        SubscriptionNotFound(persistenceId, cmd.subscriptionId))

    case DeleteUserCommand ⇒
      persist(UserDeletedEvent(persistenceId)) { _ ⇒
        handleDeletion()
        reportSuccess(true)
      }
  }

  override def notFound: Exception = UserNotFound(persistenceId)

  private def isFollowing(userId: String)(thisUser: User) =
    thisUser.subscriptions.contains(userId)

  private def isNotFollowing(userId: String)(thisUser: User) =
    !isFollowing(userId)(thisUser)
}

object UserProcessor {
  import akka.actor.Props

  def props(persistenceId: String): Props = Props(new UserProcessor(persistenceId))
}
