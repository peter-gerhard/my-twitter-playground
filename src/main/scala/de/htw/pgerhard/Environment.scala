package de.htw.pgerhard

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.htw.pgerhard.domain.tweets.{TweetConnector, TweetRepository}
import de.htw.pgerhard.domain.users.{UserConnector, UserRepository}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

trait Environment {

  implicit def actorSystem: ActorSystem
  implicit def actorMaterializer: ActorMaterializer
  implicit def executionContext: ExecutionContext

  def users: UserConnector
  def tweets: TweetConnector
}

class DefaultEnvironment extends Environment {

  override implicit def actorSystem: ActorSystem = ActorSystem("my-twitter-playground")

  override implicit def actorMaterializer: ActorMaterializer = ActorMaterializer()

  override implicit def executionContext: ExecutionContext = actorSystem.dispatcher

  implicit val timeout = Timeout(5 seconds)

  override def users: UserConnector = new UserConnector(actorSystem.actorOf(Props[UserRepository]))

  override def tweets: TweetConnector = new TweetConnector(actorSystem.actorOf(Props[TweetRepository]))
}
