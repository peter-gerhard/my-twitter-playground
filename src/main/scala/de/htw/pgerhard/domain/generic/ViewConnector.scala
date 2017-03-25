package de.htw.pgerhard.domain.generic

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait ViewConnector[A] {

  protected implicit def ec: ExecutionContext

  protected implicit def timeout: Timeout

  protected implicit def classTag: ClassTag[A]

  protected def view: ActorRef

  def getById(id: String): Future[A] =
    askView(GetById(id)).mapTo[A]

  def getOptById(id: String): Future[Option[A]] =
    askView(GetOptById(id)).mapTo[Option[A]]

  def getSeqByIds(ids: Seq[String]): Future[Seq[A]] =
    askView(GetSeqByIds(ids)).mapTo[Seq[A]]

  protected def askView(msg: Any): Future[Any] =
    ask(view, msg)
}