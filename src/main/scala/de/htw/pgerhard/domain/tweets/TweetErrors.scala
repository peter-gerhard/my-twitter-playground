package de.htw.pgerhard.domain.tweets

import de.htw.pgerhard.domain.generic.MyTwitterError

object TweetErrors {

  sealed trait TweetError extends MyTwitterError

}
