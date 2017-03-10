package de.htw.pgerhard.domain.users

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import de.htw.pgerhard.domain.users.UserCommands.CreateUserCommand

class UserRepository extends Actor with ActorLogging {

  import UserRepository._

  override def receive: Receive = {
    case cmd: CreateUserCommand ⇒
      getProcessor(randomId) forward cmd
    case Envelope(id, cmd) ⇒
      getProcessor(id) forward cmd
  }

  private def getProcessor(id: String): ActorRef =
    (context child id).getOrElse(createProcessor(id))

  private def createProcessor(id: String): ActorRef = {
    val actor = context.actorOf(Props(new UserProcessor(id)), id)
    context watch actor
    actor
  }

  private def randomId: String =
    UUID.randomUUID().toString
}

object UserRepository {
  case class Envelope(id: String, msg: Any)
}
