package de.htw.pgerhard.domain.generic

trait AggregateRoot[A] {
  def updated(event: Event[A]): A
}
