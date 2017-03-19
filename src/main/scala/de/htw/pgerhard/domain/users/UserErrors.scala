package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.generic.{Error, UserFacingError}

object UserErrors {

  // Todo messages for userfacing
  case class UserNotFound(userId: String) extends UserFacingError {
    override def getMessage(): String = s"User '$userId' not found."
  }

  case class UserAlreadyFollows(followerId: String, followingId: String) extends UserFacingError {
    override def getMessage(): String = s"User '$followerId' already follows user '$followingId'."
  }

  case class FollowerNotFound(userId: String, followerId: String) extends UserFacingError

  case class FollowingNotFound(userId: String, followingId: String) extends UserFacingError
}
