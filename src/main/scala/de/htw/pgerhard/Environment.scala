package de.htw.pgerhard

import akka.actor.{ActorSystem, Props}
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorSubscriber
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import de.htw.pgerhard.domain.generic.Event
import de.htw.pgerhard.domain.timelines._
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

  private val userViewActor = actorSystem.actorOf(UserViewActor.props)
  override val users = new UserView(userViewActor)

  private val tweetViewActor = actorSystem.actorOf(TweetViewActor.props)
  override val tweets = new TweetView(tweetViewActor)

  private val userTimelineViewActor = actorSystem.actorOf(Props(UserTimelineViewActor()))
  override val userTimelines = new UserTimelineView(userTimelineViewActor)

  private val homeTimelineViewActor = actorSystem.actorOf(Props(HomeTimelineViewActor()))
  override val homeTimelines = new HomeTimelineView(homeTimelineViewActor)

  val userRepository = new UserRepository(actorSystem.actorOf(Props(UserRepositoryActor())))
  val userCommands = new UserCommandService(userRepository)

  val tweetRepository = new TweetRepository(actorSystem.actorOf(Props(TweetRepositoryActor())))
  val tweetCommands = new TweetCommandService(users, tweets, tweetRepository)

  val readJournal: LeveldbReadJournal =
    PersistenceQuery(actorSystem)
      .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  readJournal.eventsByTag("user-event")
    .map(_.event.asInstanceOf[Event])
    .runWith(Sink.fromSubscriber(ActorSubscriber[Event](userViewActor)))

  readJournal.eventsByTag("tweet-event")
    .map(_.event.asInstanceOf[Event])
    .runWith(Sink.fromSubscriber(ActorSubscriber[Event](tweetViewActor)))

  readJournal.eventsByTag("tweet-event")
    .merge(readJournal.eventsByTag("user-deleted"))
    .map(_.event.asInstanceOf[Event])
    .runWith(Sink.fromSubscriber(ActorSubscriber[Event](userTimelineViewActor)))

  readJournal.eventsByTag("tweet-event")
    .merge(readJournal.eventsByTag("user-subscription-added"))
    .merge(readJournal.eventsByTag("user-subscription-removed"))
    .merge(readJournal.eventsByTag("user-deleted"))
    .map(_.event.asInstanceOf[Event])
    .runWith(Sink.fromSubscriber(ActorSubscriber[Event](homeTimelineViewActor)))

}
