package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.generic.Event

object UserEvents {

  sealed trait UserEvent extends Event[User]

  case class UserRegisteredEvent(id: String, handle: String, name: String) extends UserEvent

  case class UserNameSetEvent(id: String, name: String) extends UserEvent

  case class UserFollowedEvent(id: String, userId: String) extends UserEvent

  case class UserUnfollowedEvent(id: String, userId: String) extends UserEvent

  case class FollowerAddedEvent(id: String, userId: String) extends UserEvent

  case class FollowerRemovedEvent(id: String, userId: String) extends UserEvent

  case class UserDeletedEvent(id: String) extends UserEvent
}
