package de.htw.pgerhard.domain.generic

trait AggregateRoot[A, Ev <: Event] {
  def updated(ev: Ev): A
}

trait AggregateRootFactory[A <: AggregateRoot[A, Ev], Ev <: Event] {
  def fromCreatedEvent(ev: Ev): A
}
