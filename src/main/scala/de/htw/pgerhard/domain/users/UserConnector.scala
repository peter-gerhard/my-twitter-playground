package de.htw.pgerhard.domain.users

import akka.actor.ActorRef
import akka.util.Timeout
import de.htw.pgerhard.domain.generic.Connector
import de.htw.pgerhard.domain.timeline.UserTimelineConnector
import de.htw.pgerhard.domain.users.UserCommands._

import scala.concurrent.{ExecutionContext, Future}

class UserConnector(
    val repo: ActorRef,
    val timelines: UserTimelineConnector)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout)
  extends Connector[User] {

  def register(handle: String, name: String): Future[User] =
    for {
      user ← sendMessage[User](RegisterUserCommand(handle, name))
      _    ← timelines.createForUser(user.id)
    } yield user

  def setName(id: String, name: String): Future[User] =
    sendMessageTo(id, SetUserNameCommand(name))

  def addToFollowing(id: String, followingId: String): Future[User] =
    for {
      user ← sendMessageTo[User](id, FollowUserCommand(followingId))
      _    = sendMessageTo[User](followingId, AddFollowerCommand(id))
    } yield user

  def removeFromFollowing(id: String, followingId: String): Future[User] =
    for {
      user ← sendMessageTo[User](id, UnfollowUserCommand(followingId))
      _    = sendMessageTo[User](followingId, UnfollowUserCommand(followingId))
    } yield user

  def delete(id: String): Future[Boolean] =
    for {
      _   ← timelines.deleteForUser(id)
      _ ← sendMessageTo[Unit](id, DeleteUserCommand)
    } yield true
}
