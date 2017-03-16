package de.htw.pgerhard.domain.users

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import de.htw.pgerhard.domain.{Envelope, Get}
import de.htw.pgerhard.domain.users.UserCommands._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class UserConnector(repo: ActorRef)(implicit ec: ExecutionContext, timeout: Timeout) {

  def getById(id: String): Future[Option[User]] =
    sendMessage(Envelope(id, Get))

  def getMultipleByIds(ids: Seq[String]): Future[Seq[User]] =
    Future.sequence(ids.map(getById)).map(_.flatten)

  def register(handle: String, name: String): Future[Option[User]] =
    sendMessage(RegisterUserCommand(handle, name))

  def setName(id: String, name: String): Future[Option[User]] =
    sendMessage(Envelope(id, SetUserNameCommand(name)))

  def addToFollowing(id: String, followingId: String): Future[Option[User]] =
    sendMessage(Envelope(id, FollowUserCommand(followingId)))

  def removeFromFollowing(id: String, followingId: String): Future[Option[User]] =
    sendMessage(Envelope(id, UnfollowUserCommand(followingId)))

  def delete(id: String): Future[Option[User]] =
    sendMessage(Envelope(id, DeleteUserCommand))

  private def sendMessage(message: Any) =
    (repo ? message).mapTo[Option[User]]
}
