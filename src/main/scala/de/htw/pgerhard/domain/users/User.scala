package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.generic.{AggregateRoot, Event}
import de.htw.pgerhard.domain.users.UserEvents._

case class User(
    id: String,
    handle: String,
    name: String,
    following: Set[String],
    followers: Set[String])
  extends AggregateRoot[User] {

  override def updated(event: Event[User]): User = event match {
    case e: UserNameSetEvent ⇒ copy(name = e.name)
    case e: UserFollowedEvent ⇒ copy(following = following + e.userId)
    case e: UserUnfollowedEvent ⇒ copy(following = following - e.userId)
    case e: FollowerAddedEvent ⇒ copy(followers = followers + e.userId)
    case e: FollowerRemovedEvent ⇒ copy(followers = followers - e.userId)
    case e ⇒ throw new IllegalArgumentException(s"Event '$e' does not apply to class User")
  }
}

object User {
  def fromEvent(ev: UserRegisteredEvent) = User(ev.id, ev.handle, ev.name, Set.empty, Set.empty)
}
