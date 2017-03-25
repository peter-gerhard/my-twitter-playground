package de.htw.pgerhard.domain.users.commands

sealed trait UserCommand

case class RegisterUserCommand(handle: String, name: String) extends UserCommand

case class SetUserNameCommand(name: String) extends UserCommand

case class AddSubscriptionCommand(subscriptionId: String) extends UserCommand

case class RemoveSubscriptionCommand(subscriptionId: String) extends UserCommand

case object DeleteUserCommand extends UserCommand
