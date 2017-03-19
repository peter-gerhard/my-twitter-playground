package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.{Envelope, Get}
import de.htw.pgerhard.domain.generic.{AggregateRootProcessor, Repository}
import de.htw.pgerhard.domain.tweets.TweetCommands.CreateTweetCommand

class TweetRepository extends Repository[Tweet] {
  override def processor: (String) ⇒ AggregateRootProcessor[Tweet] =
    TweetProcessor.apply

  override def receive: Receive = {
    case cmd: CreateTweetCommand ⇒
      getProcessor(randomId) forward cmd
    case Envelope(id, cmd) ⇒
      getProcessor(id) forward cmd
  }
}
