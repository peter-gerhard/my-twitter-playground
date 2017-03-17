package de.htw.pgerhard.domain.timeline

import akka.persistence.RecoveryFailure
import de.htw.pgerhard.domain.generic.AggregateRootProcessor
import de.htw.pgerhard.domain.timeline.UserTimelineCommands._
import de.htw.pgerhard.domain.timeline.UserTimelineErrors._
import de.htw.pgerhard.domain.timeline.UserTimelineEvents._

class UserTimelineProcessor(val persistenceId: String)
  extends AggregateRootProcessor[UserTimeline, UserTimelineError] {

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
    case _: UserTimelineCommand ⇒
      reportState()
  }

  override def receiveWhenInitialized: Receive = {
    case cmd: PostTweetCommand ⇒
      persist(UserTweetedEvent(persistenceId, cmd.tweetId)) { event ⇒
        handleUpdate(event)
      }
      reportState()
    case cmd: DeleteTweetCommand ⇒
      state.filter(_.tweets.contains(cmd.tweetId)) foreach { _ ⇒
        persist(UserDeletedTweetEvent(persistenceId, cmd.tweetId)) { event ⇒
          handleUpdate(event)
        }
      }
      reportState()
    case cmd: PostRetweetCommand ⇒
      state.filter(!_.retweets.contains(cmd.tweetId)) foreach { _ ⇒
        persist(UserRetweetedEvent(persistenceId, cmd.tweetId, cmd.authorId)) { event ⇒
          handleUpdate(event)
        }
      }
    case cmd: DeleteRetweetCommand ⇒
      state.filter(_.retweets.contains(cmd.tweetId)) foreach { _ ⇒
        persist(UserDeletedRetweetEvent(persistenceId, cmd.tweetId)) { event ⇒
          handleUpdate(event)
        }
      }
      reportState()

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
