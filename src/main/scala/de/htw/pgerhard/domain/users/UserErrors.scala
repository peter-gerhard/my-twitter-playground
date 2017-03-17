package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.generic.{MyTwitterError, UserFacingError}

object UserErrors {

  sealed trait UserError extends MyTwitterError

  case class UserAlreadyFollows(followerId: String, followingId: String) extends UserError with UserFacingError

  case class FollowerNotFound(userId: String, followerId: String) extends UserError with UserFacingError

  case class FollowingNotFound(userId: String, followingId: String) extends UserError  with UserFacingError
}
