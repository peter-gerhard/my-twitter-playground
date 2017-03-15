package de.htw.pgerhard.domain.tweets

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import de.htw.pgerhard.domain.tweets.TweetCommands.PostTweetCommand

class TweetRepository extends Actor with ActorLogging {

  import TweetRepository._

  override def receive: Receive = {
    case cmd: PostTweetCommand ⇒
      getProcessor(randomId) forward cmd
    case Envelope(id, cmd) ⇒
      getProcessor(id) forward cmd
  }

  private def getProcessor(id: String): ActorRef =
    (context child id).getOrElse(createProcessor(id))

  private def createProcessor(id: String): ActorRef = {
    val actor = context.actorOf(Props(new TweetProcessor(id)), id)
    context watch actor
    actor
  }

  private def randomId: String =
    UUID.randomUUID().toString
}

object TweetRepository {
  case class Envelope(id: String, msg: Any)
}

