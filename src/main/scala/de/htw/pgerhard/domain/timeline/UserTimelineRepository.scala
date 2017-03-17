package de.htw.pgerhard.domain.timeline

import akka.actor.Props
import de.htw.pgerhard.domain.{Envelope, Get}
import de.htw.pgerhard.domain.generic.{AggregateRootProcessor, Repository}
import de.htw.pgerhard.domain.timeline.UserTimelineCommands.CreateUserTimelineCommand
import de.htw.pgerhard.domain.timeline.UserTimelineErrors.UserTimelineError

class UserTimelineRepository extends Repository[UserTimeline, UserTimelineError] {

  override def processor: (String) ⇒ AggregateRootProcessor[UserTimeline] =
    UserTimelineProcessor.apply

  override def viewProps(persistenceId: String, viewId: String): Props =
    Props(UserTimelineView(persistenceId, viewId))

  override def receive: Receive = {
    case Envelope(id, Get) ⇒
      getView(id) forward Get
    case cmd: CreateUserTimelineCommand ⇒
      getProcessor(randomId) forward cmd
    case Envelope(id, cmd) ⇒
      getProcessor(id) forward cmd
  }
}
