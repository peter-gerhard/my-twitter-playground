package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.generic.Event

object UserEvents {

  sealed trait UserEvent extends Event[User]

  case class UserRegisteredEvent(userId: String, handle: String, name: String) extends UserEvent

  case class UserNameSetEvent(userId: String, name: String) extends UserEvent

  case class UserAddedToFollowingEvent(userId: String, followingId: String) extends UserEvent

  case class UserRemovedFromFollowingEvent(userId: String, followingId: String) extends UserEvent

  case class UserAddedToFollowersEvent(userId: String, followerId: String) extends UserEvent

  case class UserRemovedFromFollowersEvent(userId: String, followerId: String) extends UserEvent

  case class UserDeletedEvent(userId: String) extends UserEvent

}
