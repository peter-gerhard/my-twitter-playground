package de.htw.pgerhard.domain.home

import akka.actor.ActorSystem
import de.htw.pgerhard.domain.timeline.{UserTimelineService, UserTimelineProjection}
import de.htw.pgerhard.domain.tweets.{Tweet, TweetService}
import de.htw.pgerhard.domain.users.UserService

import scala.concurrent.{ExecutionContext, Future}

class HomeTimelineService(
    users: UserService,
    tweets: TweetService,
    timelines: UserTimelineService)(
  implicit
    actorSystem: ActorSystem,
    ec: ExecutionContext) {

  // Todo future Seq[WithTweet] needed
  def getForUser(userId: String): Future[Seq[Tweet]] = {
    for {
      user      ← users.getById(userId)
      timelines ← Future.sequence(user.following.map(timelines.getForUser))
      allTweets   ← getTweets(timelines)
    } yield allTweets.sortBy(_.timestamp)
  }

  def getTweets(timelines: Set[UserTimelineProjection]): Future[Seq[Tweet]] =
    tweets.getMultipleByIds(timelines.flatMap(_.tweets).toList.map(_.tweetId))
}