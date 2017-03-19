package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.generic.Event

object TweetEvents {

  sealed trait TweetEvent extends Event[Tweet]

  case class TweetCreatedEvent(id: String, authorId: String, body: String, timestamp: Long) extends TweetEvent

  case class RetweeterAddedEvent(id: String, userId: String) extends TweetEvent

  case class RetweeterRemovedEvent(id: String, userId: String) extends TweetEvent

  case class LikerAddedEvent(id: String, userId: String) extends TweetEvent

  case class LikerRemovedEvent(id: String, userId: String) extends TweetEvent

  case class TweetDeletedEvent(id: String) extends TweetEvent
}