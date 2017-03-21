package de.htw.pgerhard.domain.timeline

import akka.actor.Props
import de.htw.pgerhard.domain.{Envelope, Get, GetOpt}
import de.htw.pgerhard.domain.generic.{AggregateRootProcessor, Repository}

class UserTimelineRepository extends Repository[UserTimeline] {

  override def processor: (String) ⇒ AggregateRootProcessor[UserTimeline] =
    UserTimelineProcessor.apply

  override def viewProps(persistenceId: String, viewId: String): Props =
    Props(UserTimelineView(persistenceId, viewId))

  override def receive: Receive = {
    case Envelope(id, GetView) ⇒
      getView(id) forward Get
    case Envelope(id, msg) ⇒
      getProcessor(id) forward msg
  }
}

case object GetView
