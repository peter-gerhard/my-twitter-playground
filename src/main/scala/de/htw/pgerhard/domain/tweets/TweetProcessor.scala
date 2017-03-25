package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.generic.{AggregateRootFactory, AggregateRootProcessor}
import de.htw.pgerhard.domain.tweets.commands._
import de.htw.pgerhard.domain.tweets.errors._
import de.htw.pgerhard.domain.tweets.events._

class TweetProcessor(val persistenceId: String) extends AggregateRootProcessor[Tweet, TweetEvent] {

  override def factory: AggregateRootFactory[Tweet, TweetEvent] = Tweet

  override def receiveRecover: Receive = {
    case ev: TweetPostedEvent  ⇒ handleCreation(ev)
    case ev: TweetDeletedEvent ⇒ handleDeletion()
    case ev: TweetEvent        ⇒ handleUpdate(ev)
  }

  override def uninitialized: Receive = {
    case cmd: PostTweetCommand ⇒
      persist(TweetPostedEvent(persistenceId, cmd.authorId, cmd.body, currentTimestamp)) { ev ⇒
        handleCreation(ev)
        reportState()
      }

    case _ ⇒
      reportFailure(notFound)
  }

  override def initialized: Receive = {
    case cmd: PostTweetCommand ⇒
      reportFailure(TweetAlreadyExists(persistenceId))

    case cmd: RepostTweetCommand ⇒
      persistUpdateIf(userHasNotRetweeted(cmd.userId))(
        TweetRepostedEvent(persistenceId, cmd.authorId, cmd.userId),
        DuplicateRepost(persistenceId, cmd.userId))

    case cmd: DeleteRepostCommand ⇒
      persistUpdateIf(userHasRetweeted(cmd.userId))(
        TweetRepostDeletedEvent(persistenceId, cmd.userId),
        RepostNotFound(persistenceId, cmd.userId))

    case cmd: DeleteTweetCommand ⇒
      persist(TweetDeletedEvent(persistenceId, cmd.authorId, cmd.repostedBy)) { _ ⇒
        handleDeletion()
        reportSuccess(true)
      }
  }

  override def notFound: Exception = TweetNotFound(persistenceId)

  private def userHasRetweeted(userId: String)(thisTweet: Tweet) =
    thisTweet.repostedBy.contains(userId)

  private def userHasNotRetweeted(userId: String)(thisTweet: Tweet) =
    !userHasRetweeted(userId)(thisTweet)

  private def currentTimestamp: Long = System.currentTimeMillis()
}

object TweetProcessor {
  import akka.actor.Props

  def props(persistenceId: String): Props = Props(new TweetProcessor(persistenceId))
}
