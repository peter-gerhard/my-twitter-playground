package de.htw.pgerhard.domain.generic

import akka.persistence.PersistentView
import de.htw.pgerhard.domain.Get

trait View[A] extends PersistentView {

  private var state: Option[A] = None

  def receiveEvent: Receive

  private def default: Receive = {
    case Get ⇒
      sender() ! state
  }

  override def receive: Receive = receiveEvent orElse default

  def setState(newState: Option[A]): Unit =
    state = newState

  def alterState(fn: A ⇒ A): Unit =
    state = state.map(fn)
}
