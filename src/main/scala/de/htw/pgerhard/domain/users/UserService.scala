package de.htw.pgerhard.domain.users

import akka.actor.ActorRef
import akka.util.Timeout
import de.htw.pgerhard.domain.generic.Connector
import de.htw.pgerhard.domain.timeline.UserTimelineService
import de.htw.pgerhard.domain.users.UserCommands._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class UserService(
    val repo: ActorRef,
    val timelines: UserTimelineService)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout,
    val ct: ClassTag[User])
  extends Connector[User] {

  def register(handle: String, name: String): Future[User] =
    for {
      user ← sendMessage(RegisterUserCommand(handle, name)).mapTo[User]
      tl   ← timelines.createForUser(user.id)
      _    = println(tl.id)
    } yield user

  def setName(id: String, name: String): Future[User] =
    sendMessageTo(id, SetUserNameCommand(name)).mapTo[User]

  def addToFollowing(id: String, followingId: String): Future[User] =
    for {
      user ← sendMessageTo(id, FollowUserCommand(followingId)).mapTo[User]
      _    ← sendMessageTo(followingId, AddFollowerCommand(id)).mapTo[User]
    } yield user

  def removeFromFollowing(id: String, followingId: String): Future[User] =
    for {
      user ← sendMessageTo(id, UnfollowUserCommand(followingId)).mapTo[User]
      _    ← sendMessageTo(followingId, RemoveFollowerCommand(id)).mapTo[User]
    } yield user

  def delete(id: String): Future[Boolean] =
    for {
      _   ← timelines.deleteForUser(id)
      res ← sendMessageTo(id, DeleteUserCommand).mapTo[Boolean]
    } yield res
}
