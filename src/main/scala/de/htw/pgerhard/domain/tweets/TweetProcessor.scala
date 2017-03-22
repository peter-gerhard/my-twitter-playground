package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.generic.AggregateRootProcessor
import de.htw.pgerhard.domain.tweets.TweetCommands._
import de.htw.pgerhard.domain.tweets.TweetErrors._
import de.htw.pgerhard.domain.tweets.TweetEvents._

class TweetProcessor(val persistenceId: String) extends AggregateRootProcessor[Tweet] {

  override type CreatedEvent = TweetCreatedEvent

  override def aggregateRootFactory: (TweetCreatedEvent) => Tweet = Tweet.fromEvent

  override def receiveRecover: Receive = {
    case event: TweetCreatedEvent ⇒
      handleCreation(event)
    case event: TweetEvent ⇒
      handleUpdate(event)
    case TweetDeletedEvent ⇒
      handleDeletion()
  }

  override def receiveBeforeInitialization: Receive = {
    case CreateTweetCommand(authorId, body) ⇒
      persist(TweetCreatedEvent(persistenceId, authorId, body, timestamp)) { event ⇒
        handleCreation(event)
        reportState()
      }

    case _: TweetCommand ⇒
      reportState()
  }

  override def receiveWhenInitialized: Receive = {
    case cmd: AddRetweeterCommand ⇒
      persistUpdateIf(userHasNotRetweeted(cmd.userId))(
        RetweeterAddedEvent(persistenceId, cmd.userId),
        DuplicateRetweet(persistenceId, cmd.userId))

    case cmd: RemoveRetweeterCommand ⇒
      persistUpdateIf(userHasRetweeted(cmd.userId))(
        RetweeterRemovedEvent(persistenceId, cmd.userId),
        RetweeterNotFound(persistenceId, cmd.userId))

    case cmd: AddLikerCommand ⇒
      persistUpdateIf(userHasNotLiked(cmd.userId))(
        LikerAddedEvent(persistenceId, cmd.userId),
        DuplicateLike(persistenceId, cmd.userId))

    case cmd: RemoveLikerCommand ⇒
      persistUpdateIf(userHasLiked(cmd.userId))(
        LikerRemovedEvent(persistenceId, cmd.userId),
        LikerNotFound(persistenceId, cmd.userId))

    case DeleteTweetCommand ⇒
      persist(TweetDeletedEvent(persistenceId)) { _ ⇒
        handleDeletion()
        reportSuccess(true)
      }
  }

  private def timestamp = System.currentTimeMillis()

  private def userHasRetweeted(userId: String)(thisTweet: Tweet) =
    thisTweet.retweeters.contains(userId)

  private def userHasNotRetweeted(userId: String)(thisTweet: Tweet) =
    !userHasRetweeted(userId)(thisTweet)

  private def userHasLiked(userId: String)(thisTweet: Tweet) =
    thisTweet.likers.contains(userId)

  private def userHasNotLiked(userId: String)(thisTweet: Tweet) =
    !userHasLiked(userId)(thisTweet)

  override def notFound(id: String): Exception = TweetNotFound(id)
}

object TweetProcessor {
  def apply(persistenceId: String) = new TweetProcessor(persistenceId)
}