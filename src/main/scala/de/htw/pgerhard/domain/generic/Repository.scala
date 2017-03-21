package de.htw.pgerhard.domain.generic

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

trait Repository[A <: AggregateRoot[A]] extends Actor with ActorLogging {

  def processor: (String) ⇒ AggregateRootProcessor[A]

  /**
    *  Override in subclasses if processing and view side are separated
    */
  protected def viewProps(persistenceId: String, viewId: String): Props = ???

  protected def getView(id: String): ActorRef = {
    val vid = viewId(id)
    (context child vid).getOrElse(createView(id, vid))
  }

  private def createView(id: String, viewId: String): ActorRef = {
    val actor = context.actorOf(viewProps(id, viewId), viewId)
    context watch actor
    actor
  }

  protected def getProcessor(id: String): ActorRef =
    (context child id).getOrElse(createProcessor(id))

  private def createProcessor(id: String): ActorRef = {
    val actor = context.actorOf(Props(processor(id)), id)
    context watch actor
    actor
  }

  protected def randomId: String =
    UUID.randomUUID().toString

  private def viewId(persistenceId: String): String =
    s"view-$persistenceId"
}
