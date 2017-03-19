package de.htw.pgerhard.domain.generic

import akka.actor.ActorLogging
import akka.actor.Status.{Failure, Success}
import akka.persistence.{PersistenceFailure, PersistentActor}
import de.htw.pgerhard.domain.{Get, GetOpt}

trait AggregateRootProcessor[A <: AggregateRoot[A]] extends PersistentActor with ActorLogging {

  type CreatedEvent <: Event[A]

  def persistenceId: String
  def aggregateRootFactory: (CreatedEvent) ⇒ A
  def receiveRecover: Receive
  def receiveBeforeInitialization: Receive
  def receiveWhenInitialized: Receive
  def notFound(id: String): Exception

  var state: Option[A] = None

  override def receiveCommand: Receive = receiveBeforeInitialization orElse default

  private def default: Receive = {
    case Get ⇒
      reportState()
    case GetOpt ⇒
      reportSuccess(state)
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

  private def becomeInitialized(): Unit =
    context.become(receiveWhenInitialized orElse default)

  private def becomeUninitialized(): Unit =
    context.become(receiveBeforeInitialization orElse default)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.debug(reason.getMessage)
    super.preRestart(reason, message)
  }

  protected def persistUpdate(event: Event[A], result: A ⇒ Any = identity): Unit =
    state
      .fold(reportFailure(notFound(persistenceId))) { s ⇒
        persist(event) { event ⇒
          handleUpdate(event)
          reportSuccess(result(s))
        }
      }

  protected def persistUpdateIf(condition: A ⇒ Boolean)(event: ⇒ Event[A], error: ⇒ Exception, result: A ⇒ Any = identity): Unit =
    state.filter(condition)
      .fold(reportFailure(error)) { s ⇒
        persist(event) { event ⇒
          handleUpdate(event)
          reportSuccess(result(s))
        }
      }

  protected def reportSuccess(result: Any): Unit =
    sender ! Success(result)

  protected def reportFailure(e: Exception): Unit =
    sender ! Failure(e)

  protected def reportState(): Unit =
    state.fold(reportFailure(notFound(persistenceId)))(reportSuccess)
}
