package de.htw.pgerhard.domain.timeline

import de.htw.pgerhard.domain.Envelope
import de.htw.pgerhard.domain.generic.{AggregateRootProcessor, Repository}
import de.htw.pgerhard.domain.timeline.UserTimelineCommands.CreateUserTimelineCommand
import de.htw.pgerhard.domain.timeline.UserTimelineErrors.UserTimelineError

class UserTimelineRepository extends Repository[UserTimeline, UserTimelineError] {

  override def processor: (String) ⇒ AggregateRootProcessor[UserTimeline, UserTimelineError] =
    UserTimelineProcessor.apply

  override def receive: Receive = {
    case cmd: CreateUserTimelineCommand ⇒
      getProcessor(randomId) forward cmd
    case Envelope(id, cmd) ⇒
      getProcessor(id) forward cmd
  }
}
