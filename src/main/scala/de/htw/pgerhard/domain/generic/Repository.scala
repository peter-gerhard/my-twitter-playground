package de.htw.pgerhard.domain.generic

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}

trait Repository extends Actor {

  protected def childProps(id: String): Props

  protected def randomId: String =
    UUID.randomUUID.toString

  protected def getChild(id: String): ActorRef =
    context.child(id).getOrElse(createChild(id))

  private def createChild(id: String): ActorRef =
    context watch context.actorOf(childProps(id), id)
}

case class Envelope(id: String, msg: Any)
