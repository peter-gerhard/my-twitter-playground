package de.htw.pgerhard

import akka.persistence.journal.{Tagged, WriteEventAdapter}
import de.htw.pgerhard.domain.users.events._
import de.htw.pgerhard.domain.tweets.events._

class TaggingEventAdapter extends WriteEventAdapter {

  override def toJournal(event: Any): Any = event match {

    case UserRegisteredEvent          ⇒ Tagged(event, Set("user-event", "user-registered"))
    case UserNameSetEvent             ⇒ Tagged(event, Set("user-event", "user-name-set"))
    case UserSubscriptionAddedEvent   ⇒ Tagged(event, Set("user-event", "user-subscription-added"))
    case UserSubscriptionRemovedEvent ⇒ Tagged(event, Set("user-event", "user-subscription-removed"))
    case UserDeletedEvent             ⇒ Tagged(event, Set("user-event", "user-deleted"))

    case TweetPostedEvent             ⇒ Tagged(event, Set("tweet-event", "tweet-posted"))
    case TweetRepostedEvent           ⇒ Tagged(event, Set("tweet-event", "tweet-reposted"))
    case TweetRepostDeletedEvent      ⇒ Tagged(event, Set("tweet-event", "tweet-repost-deleted"))
    case TweetLikedEvent              ⇒ Tagged(event, Set("tweet-event", "tweet-liked"))
    case TweetUnLikedEvent            ⇒ Tagged(event, Set("tweet-event", "tweet-unliked"))
    case TweetDeletedEvent            ⇒ Tagged(event, Set("tweet-event", "tweet-deleted"))

    case _ ⇒ event
  }

  override def manifest(event: Any): String = ""
}