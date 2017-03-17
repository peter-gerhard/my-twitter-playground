package de.htw.pgerhard.domain.tweets

import akka.actor.Props
import de.htw.pgerhard.domain.{Envelope, Get}
import de.htw.pgerhard.domain.generic.{AggregateRootProcessor, Repository}
import de.htw.pgerhard.domain.tweets.TweetCommands.CreateTweetCommand
import de.htw.pgerhard.domain.tweets.TweetErrors.TweetError

class TweetRepository extends Repository[Tweet, TweetError] {
  override def processor: (String) ⇒ AggregateRootProcessor[Tweet] =
    TweetProcessor.apply

  override def viewProps(persistenceId: String, viewId: String): Props =
    Props(TweetView(persistenceId, viewId))

  override def receive: Receive = {
    case Envelope(id, Get) ⇒
      getView(id) forward Get
    case cmd: CreateTweetCommand ⇒
      getProcessor(randomId) forward cmd
    case Envelope(id, cmd) ⇒
      getProcessor(id) forward cmd
  }
}
