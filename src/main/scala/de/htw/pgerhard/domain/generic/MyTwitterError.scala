package de.htw.pgerhard.domain.generic

import sangria.execution.{UserFacingError ⇒ SangriaUserFacingError}

trait MyTwitterError extends Exception

trait UserFacingError extends SangriaUserFacingError {
  this: MyTwitterError ⇒
}