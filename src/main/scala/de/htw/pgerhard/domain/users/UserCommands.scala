package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.generic.Command

object UserCommands {

  sealed trait UserCommand extends Command[User]

  case class RegisterUserCommand(handle: String, name: String) extends UserCommand

  case class SetUserNameCommand(name: String) extends UserCommand

  case class AddToFollowingCommand(userId: String) extends UserCommand

  case class RemoveFromFollowingCommand(userId: String) extends UserCommand

  case class AddToFollowersCommand(userId: String) extends UserCommand

  case class RemoveFromFollowersCommand(userId: String) extends UserCommand

  case object DeleteUserCommand extends UserCommand
}
