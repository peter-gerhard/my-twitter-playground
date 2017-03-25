package de.htw.pgerhard

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.htw.pgerhard.domain.timelines.{HomeTimelineView, HomeTimelineViewActor, UserTimelineView, UserTimelineViewActor}
import de.htw.pgerhard.domain.tweets._
import de.htw.pgerhard.domain.users._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

trait Environment {

  implicit def actorSystem: ActorSystem
  implicit def actorMaterializer: ActorMaterializer
  implicit def executionContext: ExecutionContext

  def users: UserView
  def tweets: TweetView
  def userTimelines: UserTimelineView
  def homeTimelines: HomeTimelineView

  def userCommands: UserCommandService
  def tweetCommands: TweetCommandService

}

class DefaultEnvironment extends Environment {

  override implicit val actorSystem: ActorSystem = ActorSystem("my-twitter-playground")

  override implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  override implicit val executionContext: ExecutionContext = actorSystem.dispatcher

  implicit val timeout = Timeout(5 seconds)

  override val users = new UserView(actorSystem.actorOf(UserViewActor.props))

  override val tweets = new TweetView(actorSystem.actorOf(TweetViewActor.props))

  override val userTimelines = new UserTimelineView(actorSystem.actorOf(UserTimelineViewActor.props))

  override val homeTimelines = new HomeTimelineView(actorSystem.actorOf(HomeTimelineViewActor.props))


  val userRepository = new UserRepository(actorSystem.actorOf(UserRepositoryActor.props))
  val userCommands = new UserCommandService(userRepository)

  val tweetRepository = new TweetRepository(actorSystem.actorOf(TweetRepositoryActor.props))
  val tweetCommands = new TweetCommandService(users, tweets, tweetRepository)
}
