package de.htw.pgerhard.domain.tweets

object TweetCommands {

  trait TweetCommand

  case class CreateTweetCommand(authorId: String, body: String) extends TweetCommand
  case class RetweetTweetCommand(userId: String) extends TweetCommand
  case class UndoRetweetTweetCommand(userId: String) extends TweetCommand
  case class LikeTweetCommand(userId: String) extends TweetCommand
  case class UndoLikeTweetCommand(userId: String) extends TweetCommand
  case object DeleteTweetCommand extends TweetCommand
}
