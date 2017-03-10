package de.htw.pgerhard.domain.users

object UserEvents {

  trait UserUpdatedEvent

  case class UserCreatedEvent(userId: String, handle: String, name: String)
  case class UserNameSetEvent(userId: String, name: String) extends UserUpdatedEvent
  case class UserFollowedEvent(userId: String, name: String) extends UserUpdatedEvent
  case class UserDeletedEvent(userId: String)

}
