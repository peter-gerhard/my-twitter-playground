package de.htw.pgerhard.util

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

/**
  * A type to chain functions of type A => Future[Option[B] ]
  */
case class FutureOption[A](private val in: Future[Option[A]]) {

  def map[B](fn: A ⇒ B)(implicit ec: ExecutionContext): FutureOption[B] =
    FutureOption(in.map(_.map(fn)))

  def flatMap[B](fn: A ⇒ FutureOption[B])(implicit ec: ExecutionContext): FutureOption[B] =
    FutureOption(
      in flatMap {
        case Some(x) ⇒ fn(x).get
        case None ⇒ Future.successful(None)
      }
    )

  def get: Future[Option[A]] = in
}

object FutureOption {
  implicit def unwrap[A](fo: FutureOption[A]): Future[Option[A]] = fo.get
}
