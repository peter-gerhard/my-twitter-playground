package de.htw.pgerhard.domain.users


import scala.concurrent.{ExecutionContext, Future}

class UserCommandService(
    private val userRepository: UserRepository)(
  implicit
    private val ec: ExecutionContext) {

  def registerUser(handle: String, name: String): Future[User] =
    userRepository.registerUser(handle, name)

  def setUserName(userId: String, name: String): Future[User] =
    userRepository.setUserName(userId, name)

  def addSubscription(userId: String, subscriptionId: String): Future[User] =
    userRepository.addSubscription(userId, subscriptionId)

  def removeSubscription(userId: String, subscriptionId: String): Future[User] =
    userRepository.removeSubscription(userId, subscriptionId)

  def deleteUser(userId: String): Future[Boolean] =
    userRepository.deleteUser(userId)
}
