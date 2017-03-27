package de.htw.pgerhard

import akka.persistence.journal.{Tagged, WriteEventAdapter}
import de.htw.pgerhard.domain.users.events._
import de.htw.pgerhard.domain.tweets.events._

class TaggingEventAdapter extends WriteEventAdapter {

  override def toJournal(event: Any): Any = event match {

    case ev: UserRegisteredEvent          ⇒ Tagged(event, Set("user-event", "user-registered"))
    case ev: UserNameSetEvent             ⇒ Tagged(event, Set("user-event", "user-name-set"))
    case ev: UserSubscriptionAddedEvent   ⇒ Tagged(event, Set("user-event", "user-subscription-added"))
    case ev: UserSubscriptionRemovedEvent ⇒ Tagged(event, Set("user-event", "user-subscription-removed"))
    case ev: UserDeletedEvent             ⇒ Tagged(event, Set("user-event", "user-deleted"))

    case ev: TweetPostedEvent             ⇒ Tagged(event, Set("tweet-event", "tweet-posted"))
    case ev: TweetRepostedEvent           ⇒ Tagged(event, Set("tweet-event", "tweet-reposted"))
    case ev: TweetRepostDeletedEvent      ⇒ Tagged(event, Set("tweet-event", "tweet-repost-deleted"))
//    case ev: TweetLikedEvent              ⇒ Tagged(event, Set("tweet-event", "tweet-liked"))
//    case ev: TweetUnLikedEvent            ⇒ Tagged(event, Set("tweet-event", "tweet-unliked"))
    case ev: TweetDeletedEvent            ⇒ Tagged(event, Set("tweet-event", "tweet-deleted"))

    case _ ⇒ event
  }

  override def manifest(event: Any): String = ""
}