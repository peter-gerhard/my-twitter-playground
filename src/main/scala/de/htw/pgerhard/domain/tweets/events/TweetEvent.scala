package de.htw.pgerhard.domain.tweets.events

import de.htw.pgerhard.domain.generic.Event

sealed trait TweetEvent extends Event

case class TweetPostedEvent(tweetId: String, authorId: String, body: String, timestamp: Long) extends TweetEvent

case class TweetRepostedEvent(tweetId: String, authorId: String, userId: String) extends TweetEvent

case class TweetRepostDeletedEvent(tweetId: String, userId: String) extends TweetEvent

case class TweetLikedEvent(tweetId: String, userId: String) extends TweetEvent

case class TweetUnLikedEvent(tweetId: String, userId: String) extends TweetEvent

case class TweetDeletedEvent(tweetId: String, authorId: String, repostedBy: Set[String]) extends TweetEvent