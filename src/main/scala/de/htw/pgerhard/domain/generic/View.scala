package de.htw.pgerhard.domain.generic

import akka.actor.ActorLogging
import akka.stream.actor.ActorSubscriberMessage._
import akka.stream.actor.{ActorSubscriber, OneByOneRequestStrategy, RequestStrategy}

trait View extends ActorSubscriber with ActorLogging {

  type EventHandler = PartialFunction[Event, Unit]

  protected def handleEvent: EventHandler

  protected def receiveClientMessage: Receive

  override protected def requestStrategy: RequestStrategy =
    OneByOneRequestStrategy

  override def receive: Receive =
    receiveEventMessage orElse receiveClientMessage

  private def receiveEventMessage: Receive = {
    case msg: OnNext ⇒
      val ev: Event = msg.element.asInstanceOf[Event]
      if (handleEvent.isDefinedAt(ev)) handleEvent(ev)

    case msg: OnError ⇒
      log.debug(msg.cause.getMessage)

    case OnComplete ⇒
      log.debug("Stream to view completed unexpectedly.")
  }
}

case class GetById(id: String)
case class GetOptById(id: String)
case class GetSeqByIds(ids: Seq[String])
