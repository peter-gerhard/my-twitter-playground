package de.htw.pgerhard.domain.users

import akka.persistence.RecoveryFailure
import de.htw.pgerhard.domain.generic.AggregateRootProcessor
import de.htw.pgerhard.domain.users.UserCommands._
import de.htw.pgerhard.domain.users.UserErrors._
import de.htw.pgerhard.domain.users.UserEvents._

class UserProcessor(override val persistenceId: String) extends AggregateRootProcessor[User] {

  override type CreatedEvent = UserRegisteredEvent

  override def aggregateRootFactory: UserRegisteredEvent => User = User.fromEvent

  override def notFound(id: String) = UserNotFound(id)

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
      persistUpdate(UserNameSetEvent(persistenceId, cmd.name))

    case cmd: FollowUserCommand ⇒
      persistUpdateIf(isNotFollowing(cmd.userId))(
        UserFollowedEvent(persistenceId, cmd.userId),
        UserAlreadyFollows(persistenceId, cmd.userId))

    case cmd: UnfollowUserCommand ⇒
      persistUpdateIf(isFollowing(cmd.userId))(
        UserUnfollowedEvent(persistenceId, cmd.userId),
        FollowingNotFound(persistenceId, cmd.userId))

    case cmd: AddFollowerCommand ⇒
      persistUpdateIf(isNotFollower(cmd.userId))(
        FollowerAddedEvent(persistenceId, cmd.userId),
        UserAlreadyFollows(cmd.userId, persistenceId))

    case cmd: RemoveFollowerCommand ⇒
      persistUpdateIf(isFollower(cmd.userId))(
        FollowerRemovedEvent(persistenceId, cmd.userId),
        FollowerNotFound(persistenceId, cmd.userId))

    case DeleteUserCommand ⇒
      persist(UserDeletedEvent(persistenceId)) { _ ⇒
        handleDeletion()
        reportSuccess(true)
      }
  }

  private def isFollowing(userId: String)(thisUser: User) =
    thisUser.following.contains(userId)

  private def isNotFollowing(userId: String)(thisUser: User) =
    !isFollowing(userId)(thisUser)

  private def isFollower(userId: String)(thisUser: User) =
    thisUser.followers.contains(userId)

  private def isNotFollower(userId: String)(thisUser: User) =
    !isFollower(userId)(thisUser)
}

object UserProcessor {
  def apply(id: String) = new UserProcessor(id)
}
