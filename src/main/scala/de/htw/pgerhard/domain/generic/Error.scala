package de.htw.pgerhard.domain.generic

import sangria.execution.{UserFacingError ⇒ SangriaUserFacingError}

trait MyTwitterError extends Exception {
  def getMessage: String
}

trait UserFacingError extends SangriaUserFacingError