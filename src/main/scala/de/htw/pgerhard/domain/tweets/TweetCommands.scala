package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.generic.Command

object TweetCommands {

  sealed trait TweetCommand extends Command[Tweet]

  case class CreateTweetCommand(authorId: String, body: String) extends TweetCommand

//  case class AddRetweeterCommand(userId: String) extends TweetCommand
//
//  case class RemoveRetweeterCommand(userId: String) extends TweetCommand
//
//  case class AddLikerCommand(userId: String) extends TweetCommand
//
//  case class RemoveLikerCommand(userId: String) extends TweetCommand

  case object DeleteTweetCommand extends TweetCommand
}
