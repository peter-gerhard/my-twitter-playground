package de.htw.pgerhard.domain.generic

import akka.actor.ActorLogging
import akka.persistence.{PersistenceFailure, PersistentActor}
import de.htw.pgerhard.domain.Get

trait AggregateRootProcessor[A <: AggregateRoot[A]] extends PersistentActor with ActorLogging {

  type CreatedEvent <: Event[A]

  def persistenceId: String
  def aggregateRootFactory: (CreatedEvent) ⇒ A
  def receiveBeforeInitialization: Receive
  def receiveWhenInitialized: Receive

  var state: Option[A] = None

  override def receiveCommand: Receive = receiveBeforeInitialization orElse default

  private def default: Receive = {
    case Get ⇒
      reportState()
    case PersistenceFailure(payload, snr, e) =>
      println(s"persistence failed (payload = $payload, sequenceNr = $snr, error = ${e.getMessage})")
  }

  protected def handleCreation(event: CreatedEvent): Unit = {
    state = Some(aggregateRootFactory(event))
    becomeInitialized()
  }

  protected def handleUpdate(event: Event[A]): Unit = {
    state = state.map(_.updated(event))
  }

  protected def handleDeletion(): Unit = {
    state = None
    becomeUninitialized()
  }

  protected def reportState(): Unit =
    sender() ! state

  private def becomeInitialized(): Unit =
    context.become(receiveWhenInitialized orElse default)

  private def becomeUninitialized(): Unit =
    context.become(receiveBeforeInitialization orElse default)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.debug(reason.getMessage)
    super.preRestart(reason, message)
  }
}
