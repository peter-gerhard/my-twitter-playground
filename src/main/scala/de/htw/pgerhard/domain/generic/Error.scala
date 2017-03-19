package de.htw.pgerhard.domain.generic

import sangria.execution.{UserFacingError ⇒ SangriaUserFacingError}

trait Error extends Exception

trait UserFacingError extends Error with SangriaUserFacingError