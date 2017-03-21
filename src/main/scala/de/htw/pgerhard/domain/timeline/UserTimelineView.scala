package de.htw.pgerhard.domain.timeline

import de.htw.pgerhard.domain.generic.View
import de.htw.pgerhard.domain.timeline.UserTimelineErrors.UserTimelineNotFound
import de.htw.pgerhard.domain.timeline.UserTimelineEvents._

class UserTimelineView(override val persistenceId: String, override val viewId: String)
  extends View[UserTimelineProjection] {

  override def receiveEvent: Receive = {
    case ev: UserTimelineCreatedEvent ⇒
      setState(Some(UserTimelineProjection.fromEvent(ev)))
    case ev: UserTimelineDeletedEvent ⇒
      setState(None)
    case ev: UserTimelineEvent ⇒
      alterState(_.updated(ev))
  }

  override def notFound(id: String): Exception = UserTimelineNotFound(id)
}

object UserTimelineView {
  def apply(persistenceId: String, viewId: String) =
    new UserTimelineView(persistenceId, viewId)
}
