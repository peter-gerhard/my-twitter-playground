package de.htw.pgerhard.domain.tweets.commands

sealed trait TweetCommand

case class PostTweetCommand(authorId: String, body: String) extends TweetCommand

case class RepostTweetCommand(userId: String, authorId: String) extends TweetCommand

case class DeleteRepostCommand(userId: String) extends TweetCommand

case class DeleteTweetCommand(authorId: String, repostedBy: Set[String]) extends TweetCommand