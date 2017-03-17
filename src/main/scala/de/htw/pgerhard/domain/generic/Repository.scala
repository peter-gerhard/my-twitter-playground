package de.htw.pgerhard.domain.generic

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

trait Repository[A <: AggregateRoot[A], Error] extends Actor with ActorLogging {

  def processor: (String) ⇒ AggregateRootProcessor[A, Error]

  protected def getProcessor(id: String): ActorRef =
    (context child id).getOrElse(createProcessor(id))

  protected def createProcessor(id: String): ActorRef = {
    val actor = context.actorOf(Props(processor(id)), id)
    context watch actor
    actor
  }

  protected def randomId: String =
    UUID.randomUUID().toString
}
