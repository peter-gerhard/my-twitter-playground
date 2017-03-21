package de.htw.pgerhard

trait WithUser {
  this: MyTwitterFeature with MyTwitterSteps ⇒

  beforeEachScenario (
    register_a_user(handle = "@test", name = "Test User", alias = "test-user")
  )

  afterEachScenario (
    delete_user("<test-user>")
  )
}
