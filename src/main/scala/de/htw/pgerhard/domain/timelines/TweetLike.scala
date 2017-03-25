package de.htw.pgerhard.domain.timelines

case class TweetLike(tweetId: String, authorId: String, reposterId: Option[String] = None)
