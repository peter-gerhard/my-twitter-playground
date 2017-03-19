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

  def repo: ActorRef

  def getById(id: String): Future[A] =
    sendMessage(Envelope(id, Get))

  def getOptById(id: String): Future[Option[A]] =
    (repo ? Envelope(id, GetOpt)).mapTo[Option[A]]

  def getMultipleByIds(ids: Seq[String]): Future[Seq[A]] =
    Future.sequence(ids.map(id ⇒ getOptById(id))).map(_.flatten)

  protected def sendMessage[B: ClassTag](msg: Any): Future[B] =
    (repo ? msg).mapTo[B]

  protected def sendMessageTo[B: ClassTag](id: String, msg: Any): Future[B] =
    (repo ? Envelope(id, msg)).mapTo[B]
}
