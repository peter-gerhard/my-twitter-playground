package de.htw.pgerhard.domain.timeline

import de.htw.pgerhard.domain.Envelope
import de.htw.pgerhard.domain.generic.{AggregateRootProcessor, Repository}
import de.htw.pgerhard.domain.timeline.UserTimelineCommands.CreateUserTimelineCommand

class UserTimelineRepository extends Repository[UserTimeline] {

  override def processor(id: String): AggregateRootProcessor[UserTimeline] = UserTimelineProcessor(id)

  override def receive: Receive = {
    case cmd: CreateUserTimelineCommand ⇒
      getProcessor(randomId) forward cmd
    case Envelope(id, cmd) ⇒
      getProcessor(id) forward cmd
  }
}
