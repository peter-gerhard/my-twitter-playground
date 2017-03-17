package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.Envelope
import de.htw.pgerhard.domain.generic.{AggregateRootProcessor, Repository}
import de.htw.pgerhard.domain.tweets.TweetCommands.CreateTweetCommand
import de.htw.pgerhard.domain.tweets.TweetErrors.TweetError

class TweetRepository extends Repository[Tweet, TweetError] {
  override def processor: (String) ⇒ AggregateRootProcessor[Tweet, TweetError] =
    TweetProcessor.apply

  override def receive: Receive = {
    case cmd: CreateTweetCommand ⇒
      getProcessor(randomId) forward cmd
    case Envelope(id, cmd) ⇒
      getProcessor(id) forward cmd
  }
}
