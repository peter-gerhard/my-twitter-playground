package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.Envelope
import de.htw.pgerhard.domain.generic.{AggregateRootProcessor, Repository}
import de.htw.pgerhard.domain.users.UserCommands._

class UserRepository extends Repository[User] {

  override def processor(id: String): AggregateRootProcessor[User] = UserProcessor(id)

  override def receive: Receive = {
    case cmd: RegisterUserCommand ⇒
      getProcessor(randomId) forward cmd
    case Envelope(id, cmd: AddToFollowingCommand) ⇒
      getProcessor(id) forward cmd
      getProcessor(cmd.userId) ! AddToFollowersCommand(id)
    case Envelope(id, cmd) ⇒
      getProcessor(id) forward cmd
  }
}
