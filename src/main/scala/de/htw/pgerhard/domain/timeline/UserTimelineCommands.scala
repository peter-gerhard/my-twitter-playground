package de.htw.pgerhard.domain.timeline

import de.htw.pgerhard.domain.generic.Command

object UserTimelineCommands {

  sealed trait UserTimelineCommand extends Command[UserTimeline]

  case class CreateUserTimelineCommand(userId: String) extends UserTimelineCommand

  case class PostTweetCommand(tweetId: String) extends UserTimelineCommand

  case class DeleteTweetCommand(tweetId: String) extends UserTimelineCommand

  case class PostRetweetCommand(tweetId: String, authorId: String) extends UserTimelineCommand

  case class DeleteRetweetCommand(tweetId: String) extends UserTimelineCommand

  case object DeleteUserTimelineCommand extends UserTimelineCommand
}