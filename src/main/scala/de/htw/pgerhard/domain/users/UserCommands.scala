package de.htw.pgerhard.domain.users

object UserCommands {

  trait UserCommand

  case class CreateUserCommand(handle: String, name: String) extends UserCommand
  case class SetUserNameCommand(name: String) extends UserCommand
  case class FollowUserCommand(followId: String) extends UserCommand
  case object DeleteUserCommand extends UserCommand
}
