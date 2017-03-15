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
    case e: UserAddedToFollowingEvent ⇒ copy(following = following + e.followingId)
    case e: UserRemovedFromFollowingEvent ⇒ copy(following = following - e.followingId)
    case e: UserAddedToFollowersEvent ⇒ copy(followers = followers + e.followerId)
    case e: UserRemovedFromFollowersEvent ⇒ copy(followers = followers - e.followerId)
    case e ⇒ throw new IllegalArgumentException(s"Event '$e' does not apply to class User")
  }
}

object User {
  def fromEvent(ev: UserRegisteredEvent) = User(ev.userId, ev.handle, ev.name, Set.empty, Set.empty)
}
