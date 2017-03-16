package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.generic.Command

object UserCommands {

  sealed trait UserCommand extends Command[User]

  case class RegisterUserCommand(handle: String, name: String) extends UserCommand

  case class SetUserNameCommand(name: String) extends UserCommand

  case class FollowUserCommand(userId: String) extends UserCommand

  case class UnfollowUserCommand(userId: String) extends UserCommand

  case class AddFollowerCommand(userId: String) extends UserCommand

  case class RemoveFollowerCommand(userId: String) extends UserCommand

  case object DeleteUserCommand extends UserCommand
}
