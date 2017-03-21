package de.htw.pgerhard.domain.generic

import akka.actor.ActorLogging
import akka.actor.Status.{Failure, Success}
import akka.persistence.PersistentView
import de.htw.pgerhard.domain.{Get, GetOpt}

trait View[A] extends PersistentView with ActorLogging {

  private var state: Option[A] = None

  def receiveEvent: Receive

  def notFound(id: String): Exception

  private def default: Receive = {
    case Get ⇒
      reportState()
    case GetOpt ⇒
      reportSuccess(state)
  }

  override def receive: Receive = receiveEvent orElse default

  def setState(newState: Option[A]): Unit =
    state = newState

  def alterState(fn: A ⇒ A): Unit =
    state = state.map(fn)

  def reportState(): Unit =
    state.fold(reportFailure(notFound(persistenceId)))(reportSuccess)

  protected def reportSuccess(result: Any): Unit =
    sender ! Success(result)

  protected def reportFailure(e: Exception): Unit =
    sender ! Failure(e)
}
