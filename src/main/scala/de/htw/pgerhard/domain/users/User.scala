package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.users.UserEvents._

case class User(id: String, handle: String, name: String, follows: Seq[String]= Seq.empty) {

  def updated(event: UserUpdatedEvent): User = event match {
    case UserNameSetEvent(_, x) ⇒ copy(name = x)
    case UserFollowedEvent(_, x) ⇒ copy(follows = follows :+ x)
  }
}

object User {
  def fromEvent(event: UserCreatedEvent): User =
    User(event.userId, event.handle, event.name)
}