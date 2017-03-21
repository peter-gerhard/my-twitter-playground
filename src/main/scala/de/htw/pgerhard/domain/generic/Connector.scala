package de.htw.pgerhard.domain.generic

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import de.htw.pgerhard.domain.{Envelope, Get, GetOpt}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait Connector[A] {

  implicit def ec: ExecutionContext
  implicit def timeout: Timeout
  implicit def ct: ClassTag[A]

  def repo: ActorRef

  def getById(id: String): Future[A] =
    sendMessageTo(id, Get).mapTo[A]

  def getOptById(id: String): Future[Option[A]] =
    sendMessageTo(id, GetOpt).mapTo[Option[A]]

  def getMultipleByIds(ids: Seq[String]): Future[Seq[A]] =
    Future.sequence(ids.map(id ⇒ getOptById(id))).map(_.flatten)

  protected def sendMessage(msg: Any): Future[Any] =
    repo ? msg

  protected def sendMessageTo(id: String, msg: Any): Future[Any] =
    repo ? Envelope(id, msg)
}
