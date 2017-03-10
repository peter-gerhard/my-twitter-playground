package de.htw.pgerhard

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import de.htw.pgerhard.domain.tweets.{TweetContext, TweetRepository}
import de.htw.pgerhard.domain.users.{UserContext, UserRepository}

import scala.concurrent.ExecutionContext

trait Environment {

  implicit def actorSystem: ActorSystem
  implicit def actorMaterializer: ActorMaterializer
  implicit def executionContext: ExecutionContext

  def users: UserContext
  def tweets: TweetContext
}

class DefaultEnvironment extends Environment {

  override implicit def actorSystem: ActorSystem = ActorSystem("my-twitter-playground")

  override implicit def actorMaterializer: ActorMaterializer = ActorMaterializer()

  override implicit def executionContext: ExecutionContext = actorSystem.dispatcher

  override def users: UserContext = new UserContext(actorSystem.actorOf(Props[UserRepository]))

  override def tweets: TweetContext = new TweetContext(actorSystem.actorOf(Props[TweetRepository]))
}
