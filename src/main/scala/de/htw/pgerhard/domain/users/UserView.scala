package de.htw.pgerhard.domain.users

import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import de.htw.pgerhard.domain.generic._
import de.htw.pgerhard.domain.users.errors.UserNotFound
import de.htw.pgerhard.domain.users.events._

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

class UserView(
    val view: ActorRef)(
  override implicit
    val ec: ExecutionContext,
    val timeout: Timeout,
    val classTag: ClassTag[SimpleUser])
  extends ViewConnector[SimpleUser]

class UserViewActor extends View {

  private val users = mutable.Map[String, SimpleUser]()

  override protected def handleEvent: EventHandler = {
    case ev: UserRegisteredEvent ⇒
      users(ev.userId) = SimpleUser.fromCreatedEvent(ev)

    case ev: UserNameSetEvent ⇒
      update(ev.userId, ev)

    case ev: UserSubscriptionAddedEvent ⇒
      update(ev.userId, ev)
      users(ev.subscriptionId) = users(ev.subscriptionId).withAdditionalSubscriber(ev.userId)

    case ev: UserSubscriptionRemovedEvent ⇒
      update(ev.userId, ev)
      users(ev.subscriptionId) = users(ev.subscriptionId).withRemovedSubscriber(ev.userId)

    case ev: UserDeletedEvent ⇒
      users.remove(ev.userId)

    case _ ⇒
  }

  override protected def receiveClientMessage: Receive = {
    case msg: GetById ⇒
      sender() ! users.getOrElse(msg.id, UserNotFound(msg.id))

    case msg: GetOptById ⇒
      sender() ! users.get(msg.id)

    case msg: GetSeqByIds ⇒
      sender() ! msg.ids.flatMap(users.get)
  }

  private def update(id: String, ev: UserEvent) =
    users(id) = users(id).updated(ev)
}

object UserViewActor {
  def props: Props = Props(new UserViewActor)
}

case class SimpleUser(
    id: String,
    handle: String,
    name: String,
    subscriptions: Set[String],
    subscribers: Set[String]) {

  def withAdditionalSubscriber(subscriberId: String): SimpleUser =
    copy(subscribers = subscribers + subscriberId)

  def withRemovedSubscriber(subscriberId: String): SimpleUser =
    copy(subscribers = subscribers - subscriberId)

  def updated(event: UserEvent): SimpleUser = event match {
    case ev: UserNameSetEvent ⇒
      copy(name = ev.name)

    case ev: UserSubscriptionAddedEvent ⇒
      copy(subscriptions = this.subscriptions + ev.subscriptionId)

    case ev: UserSubscriptionRemovedEvent ⇒
      copy(subscriptions = this.subscriptions - ev.subscriptionId)

    case _ ⇒
      throw new IllegalArgumentException
  }
}

object SimpleUser {
  def fromCreatedEvent(ev: UserRegisteredEvent) =
    SimpleUser(ev.userId, ev.handle, ev.name, Set.empty, Set.empty)
}
