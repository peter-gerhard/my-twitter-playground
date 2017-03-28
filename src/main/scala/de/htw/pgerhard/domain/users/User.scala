package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.generic.{AggregateRoot, AggregateRootFactory}
import de.htw.pgerhard.domain.users.events._

case class User(
    id: String,
    handle: String,
    name: String,
    subscriptions: Set[String])
  extends AggregateRoot[User, UserEvent] {

  override def updated(event: UserEvent): User = event match {
    case ev: UserNameSetEvent             ⇒ copy(name = ev.name)
    case ev: UserSubscriptionAddedEvent   ⇒ copy(subscriptions = subscriptions + ev.subscriptionId)
    case ev: UserSubscriptionRemovedEvent ⇒ copy(subscriptions = subscriptions - ev.subscriptionId)
    case ev ⇒ throw new IllegalArgumentException(s"Event '$ev' does not apply to class User")
  }
}

object User extends AggregateRootFactory[User, UserEvent] {
  override def fromCreatedEvent(event: UserEvent): User = event match {
    case ev: UserRegisteredEvent ⇒
      User(ev.userId, ev.handle, ev.name, Set.empty)

    case _ ⇒
      throw new IllegalArgumentException
  }
}
