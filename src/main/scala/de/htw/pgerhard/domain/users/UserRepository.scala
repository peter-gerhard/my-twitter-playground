package de.htw.pgerhard.domain.users

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.util.Timeout
import de.htw.pgerhard.domain.generic.{Envelope, Repository, RepositoryConnector}
import de.htw.pgerhard.domain.users.commands._

import scala.concurrent.{ExecutionContext, Future}

class UserRepository(
    val repository: ActorRef)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout)
  extends RepositoryConnector {

  def registerUser(handle: String, name: String): Future[User] =
    askRepo(RegisterUserCommand(handle, name)).mapTo[User]

  def setUserName(userId: String, name: String): Future[User] =
    askRepo(userId, SetUserNameCommand(name)).mapTo[User]

  def addSubscription(userId: String, subscriptionId: String): Future[User] =
    askRepo(userId, AddSubscriptionCommand(subscriptionId)).mapTo[User]

  def removeSubscription(userId: String, subscriptionId: String): Future[User] =
    askRepo(userId, RemoveSubscriptionCommand(subscriptionId)).mapTo[User]

  def deleteUser(userId: String): Future[Boolean] =
    askRepo(userId, DeleteUserCommand).mapTo[Boolean]
}

class UserRepositoryActor extends Repository with ActorLogging {

  override protected def childProps(id: String): Props = UserProcessor.props(id)

  override def receive: Receive = {
    case cmd: RegisterUserCommand ⇒
      getChild(randomId) forward cmd

    case env: Envelope ⇒
      getChild(env.id) forward env.msg
  }
}

object UserRepositoryActor {
  def props: Props = Props(new UserRepositoryActor)
}
