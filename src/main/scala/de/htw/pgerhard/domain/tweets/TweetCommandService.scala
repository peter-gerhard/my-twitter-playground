package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.users.UserView

import scala.concurrent.{ExecutionContext, Future}

class TweetCommandService(
    private val simpleUserView: UserView,
    private val simpleTweetView: TweetView,
    private val tweetRepository: TweetRepository)(
  implicit
    private val ec: ExecutionContext) {

  def postTweet(authorId: String, body: String): Future[Tweet] =
    tweetRepository.postTweet(authorId, body)

  def repostTweet(tweetId: String, userId: String): Future[Tweet] =
    for {
      tweet  ← simpleTweetView.getById(tweetId)
      result ← tweetRepository.repostTweet(tweetId, userId, tweet.authorId)
    } yield result

  def deletRepost(tweetId: String, userId: String): Future[Tweet] =
    tweetRepository.deleteRepost(tweetId, userId)

  def deleteTweet(tweetId: String): Future[Boolean] =
    for {
      tweet  ← simpleTweetView.getById(tweetId)
      result ← tweetRepository.deleteTweet(tweetId, tweet.authorId, Set.empty) // Todo, provide reposters
    } yield result
}
