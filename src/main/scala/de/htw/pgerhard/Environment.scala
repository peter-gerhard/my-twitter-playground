package de.htw.pgerhard

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.htw.pgerhard.domain.home.HomeTimelineService
import de.htw.pgerhard.domain.timeline.{UserTimelineService, UserTimelineRepository}
import de.htw.pgerhard.domain.tweets.{TweetService, TweetRepository}
import de.htw.pgerhard.domain.users.{UserService, UserRepository}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

trait Environment {

  implicit def actorSystem: ActorSystem
  implicit def actorMaterializer: ActorMaterializer
  implicit def executionContext: ExecutionContext

  def users: UserService
  def tweets: TweetService
  def userTimelines: UserTimelineService
  def homeTimeLines: HomeTimelineService
}

class DefaultEnvironment extends Environment {

  override implicit val actorSystem: ActorSystem = ActorSystem("my-twitter-playground")

  override implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  override implicit val executionContext: ExecutionContext = actorSystem.dispatcher

  implicit val timeout = Timeout(5 seconds)

  override lazy val users: UserService = new UserService(actorSystem.actorOf(Props[UserRepository]), userTimelines)

  override lazy val tweets: TweetService = new TweetService(actorSystem.actorOf(Props[TweetRepository]))

  override lazy val userTimelines = new UserTimelineService(actorSystem.actorOf(Props[UserTimelineRepository]), tweets)

  override lazy val homeTimeLines = new HomeTimelineService(users, tweets, userTimelines)
}
