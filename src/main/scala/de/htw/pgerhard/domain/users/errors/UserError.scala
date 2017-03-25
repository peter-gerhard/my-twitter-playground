package de.htw.pgerhard.domain.users.errors

import de.htw.pgerhard.domain.generic.{MyTwitterError, UserFacingError}

sealed trait UserError extends MyTwitterError

case class UserNotFound(userId: String) extends UserError with  UserFacingError {
  override def getMessage(): String = s"User '$userId' not found."
}

case class UserAlreadyExists(userId: String) extends UserError with  UserFacingError {
  override def getMessage(): String = s"User '$userId' already exists."
}

case class UserAlreadySubscribed(userId: String, subscriptionId: String) extends UserError with UserFacingError {
  override def getMessage(): String = s"User '$userId' already subscribed to user '$subscriptionId'."
}

case class SubscriptionNotFound(userId: String, subscriptionId: String) extends UserError with UserFacingError {
  override def getMessage(): String = s"User '$userId' is not subscribed to user '$subscriptionId'."
}
