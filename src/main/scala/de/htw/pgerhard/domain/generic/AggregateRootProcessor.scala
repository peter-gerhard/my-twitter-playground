package de.htw.pgerhard.domain.generic

import akka.actor.ActorLogging
import akka.actor.Status.{Failure, Success}
import akka.persistence.PersistentActor

trait AggregateRootProcessor[A <: AggregateRoot[A, Ev], Ev <: Event]
  extends PersistentActor with ActorLogging {

  def persistenceId: String

  def factory: AggregateRootFactory[A, Ev]

  def receiveRecover: Receive

  def uninitialized: Receive

  def initialized: Receive

  def notFound: Exception

  protected var state: Option[A] = None

  override def receiveCommand: Receive = uninitialized

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.debug(reason.getMessage)
    super.preRestart(reason, message)
  }

  protected def handleCreation(ev: Ev): Unit = {
    state = Some(factory.fromCreatedEvent(ev))
    context.become(initialized)
  }

  protected def handleUpdate(ev: Ev): Unit = {
    state = state.map(_.updated(ev))
  }

  protected def handleDeletion(): Unit = {
    state = None
    context.become(uninitialized)
  }

  protected def persistUpdate(ev: Ev): Unit =
    state
      .fold(reportFailure(notFound)) { s ⇒
        persist(ev) { event ⇒
          handleUpdate(event)
          reportState()
        }
      }

  protected def persistUpdateIf(condition: A ⇒ Boolean)(ev: ⇒ Ev, error: ⇒ Exception): Unit =
    state.filter(condition)
      .fold(reportFailure(error)) { s ⇒
        persist(ev) { ev ⇒
          handleUpdate(ev)
          reportState()
        }
      }

  protected def reportSuccess(result: Any): Unit =
    sender ! Success(result)

  protected def reportFailure(e: Exception): Unit =
    sender ! Failure(e)

  protected def reportState(): Unit =
    state.fold(reportFailure(notFound))(reportSuccess)
}
