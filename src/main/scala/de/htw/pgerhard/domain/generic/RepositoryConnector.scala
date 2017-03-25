package de.htw.pgerhard.domain.generic

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}

trait RepositoryConnector {

  protected implicit def ec: ExecutionContext

  protected implicit def timeout: Timeout

  protected def repository: ActorRef

  protected def askRepo(msg: Any): Future[Any] =
    ask(repository, msg)

  protected def askRepo(id: String, msg: Any): Future[Any] =
    ask(repository, Envelope(id, msg))
}
