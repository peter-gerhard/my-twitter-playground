package de.htw.pgerhard.domain.tweets

object TweetCommands {

  trait TweetCommand

  case class PostTweetCommand(authorId: String, body: String) extends TweetCommand

  case class AddRepostCommand(fromId: String) extends TweetCommand

  case class RemoveRepostCommand(fromId: String) extends TweetCommand

  case class AddLikeCommand(fromId: String) extends TweetCommand

  case class RemoveLikeCommand(fromId: String) extends TweetCommand

  case object DeleteTweetCommand extends TweetCommand
}
