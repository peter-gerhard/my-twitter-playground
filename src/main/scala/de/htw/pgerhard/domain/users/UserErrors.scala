package de.htw.pgerhard.domain.users

object UserErrors {

  sealed trait UserError

  case class UserAlreadyFollows(followerId: String, followingId: String) extends UserError

  case class FollowerNotFound(userId: String, followerId: String) extends UserError

  case class FollowingNotFound(userId: String, followingId: String) extends UserError
}
