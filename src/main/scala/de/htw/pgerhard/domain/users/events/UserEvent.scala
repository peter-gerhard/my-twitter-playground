package de.htw.pgerhard.domain.users.events

import de.htw.pgerhard.domain.generic.Event

sealed trait UserEvent extends Event

case class UserRegisteredEvent(userId: String, handle: String, name: String) extends UserEvent

case class UserNameSetEvent(userId: String, name: String) extends UserEvent

case class UserSubscriptionAddedEvent(userId: String, subscriptionId: String) extends UserEvent

case class UserSubscriptionRemovedEvent(userId: String, subscriptionId: String) extends UserEvent

case class UserDeletedEvent(userId: String) extends UserEvent