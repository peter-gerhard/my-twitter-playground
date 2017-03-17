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
        reportResult(())
      }
    case _: UserTimelineCommand ⇒
      reportState()
  }

  override def receiveWhenInitialized: Receive = {
    case cmd: PostTweetCommand ⇒
      persistUpdateIf(tweetNotFound(cmd.tweetId))(
        UserTweetedEvent(persistenceId, cmd.tweetId))(
        AlreadyTweeted(persistenceId, cmd.tweetId))

    case cmd: DeleteTweetCommand ⇒
      persistUpdateIf(tweetFound(cmd.tweetId))(
        UserDeletedTweetEvent(persistenceId, cmd.tweetId))(
        TweetNotFound(persistenceId, cmd.tweetId))

    case cmd: PostRetweetCommand ⇒
      persistUpdateIf(retweetNotFound(cmd.tweetId))(
        UserRetweetedEvent(persistenceId, cmd.tweetId, cmd.authorId))(
        AlreadyRetweeted(persistenceId, cmd.tweetId))

    case cmd: DeleteRetweetCommand ⇒
      persistUpdateIf(retweetFound(cmd.tweetId))(
        UserDeletedRetweetEvent(persistenceId, cmd.tweetId))(
        RetweetNotFound(persistenceId, cmd.tweetId))

    case DeleteUserTimelineCommand  ⇒
      persist(UserTimelineDeletedEvent(persistenceId)) { _ ⇒
        handleDeletion()
        reportState()
      }
  }

  private def tweetFound(tweetId: String)(timeline: UserTimeline) =
    timeline.tweets.contains(tweetId)

  private def tweetNotFound(tweetId: String)(timeline: UserTimeline) =
    !tweetFound(tweetId)(timeline)

  private def retweetFound(tweetId: String)(timeline: UserTimeline) =
    timeline.retweets.contains(tweetId)

  private def retweetNotFound(tweetId: String)(timeline: UserTimeline) =
    !timeline.retweets.contains(tweetId)
}

object UserTimelineProcessor {
  def apply(id: String) = new UserTimelineProcessor(id)
}
