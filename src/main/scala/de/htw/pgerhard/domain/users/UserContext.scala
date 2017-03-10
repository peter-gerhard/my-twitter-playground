package de.htw.pgerhard.domain.users

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import de.htw.pgerhard.Get
import de.htw.pgerhard.domain.users.UserCommands._
import de.htw.pgerhard.domain.users.UserRepository.Envelope

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class UserContext(repo: ActorRef)(implicit ec: ExecutionContext) {

  private implicit val timeout = Timeout(5 seconds)

  def getById(id: String): Future[Option[User]] =
    sendMessage(Envelope(id, Get))

  def getMultipleByIds(ids: Seq[String]): Future[Seq[User]] =
    Future.sequence(ids.map(id ⇒ getById(id))).map(_.flatten)

  def create(handle: String, name: String): Future[Option[User]] =
    sendMessage(CreateUserCommand(handle, name))

  def setName(id: String, name: String): Future[Option[User]] =
    sendMessage(Envelope(id, SetUserNameCommand(name)))

  def follow(id: String, followId: String): Future[Option[User]] =
    sendMessage(Envelope(id, FollowUserCommand(followId)))

  def delete(id: String): Future[Option[User]] =
    sendMessage(Envelope(id, DeleteUserCommand))

  private def sendMessage(message: Any) =
    (repo ? message).mapTo[Option[User]]
}
