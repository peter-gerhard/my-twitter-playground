package de.htw.pgerhard.domain.users

import de.htw.pgerhard.domain.Envelope
import de.htw.pgerhard.domain.generic.{AggregateRootProcessor, Repository}
import de.htw.pgerhard.domain.users.UserCommands._
import de.htw.pgerhard.domain.users.UserErrors.UserError

class UserRepository extends Repository[User, UserError] {

  override def processor: (String) ⇒ AggregateRootProcessor[User] =
    UserProcessor.apply

  override def receive: Receive = {
    case cmd: RegisterUserCommand ⇒
      getProcessor(randomId) forward cmd
    case Envelope(id, cmd: FollowUserCommand) ⇒
      getProcessor(id) forward cmd
      getProcessor(cmd.userId) ! AddFollowerCommand(id)
    case Envelope(id, cmd) ⇒
      getProcessor(id) forward cmd
  }
}
