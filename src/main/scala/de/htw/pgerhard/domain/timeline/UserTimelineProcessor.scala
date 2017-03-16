package de.htw.pgerhard.domain.timeline

import akka.persistence.RecoveryFailure
import de.htw.pgerhard.domain.generic.AggregateRootProcessor
import de.htw.pgerhard.domain.timeline.UserTimelineCommands._
import de.htw.pgerhard.domain.timeline.UserTimelineEvents._
import de.htw.pgerhard.domain.tweets.TweetCommands.TweetCommand

class UserTimelineProcessor(val persistenceId: String) extends AggregateRootProcessor[UserTimeline] {

  override type CreatedEvent = UserTimelineCreatedEvent

  override def aggregateRootFactory: (UserTimelineCreatedEvent) => UserTimeline = UserTimeline.fromEvent

  override def receiveRecover: Receive = {
    case event: UserTimelineCreatedEvent ⇒ handleCreation(event)
    case _: UserTimelineDeletedEvent ⇒ handleDeletion()
    case event: UserTimelineEvent ⇒ handleUpdate(event)
    case RecoveryFailure(e) =>
      println(s"recovery failed (error = ${e.getMessage})")
  }

  override def receiveBeforeInitialization: Receive = {
    case cmd: CreateUserTimelineCommand ⇒
      persist(UserTimelineCreatedEvent(persistenceId, cmd.userId)) { event ⇒
        handleCreation(event)
        reportState()
      }
    case _: TweetCommand ⇒
      reportState()
  }

  override def receiveWhenInitialized: Receive = {
    case cmd: PostTweetCommand ⇒
      persist(UserTweetedEvent(persistenceId, cmd.tweetId)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case cmd: DeleteTweetCommand ⇒
      persist(UserDeletedTweetEvent(persistenceId, cmd.tweetId)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case cmd: PostRetweetCommand ⇒
      persist(UserRetweetedEvent(persistenceId, cmd.tweetId, cmd.authorId)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case cmd: DeleteRetweetCommand ⇒
      persist(UserDeletedRetweetEvent(persistenceId, cmd.tweetId)) { event ⇒
        handleUpdate(event)
        reportState()
      }
    case DeleteUserTimeLineCommand  ⇒
      persist(UserTimelineDeletedEvent(persistenceId)) { _ ⇒
        handleDeletion()
        reportState()
      }
  }
}

object UserTimelineProcessor {
  def apply(id: String) = new UserTimelineProcessor(id)
}
