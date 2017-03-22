package de.htw.pgerhard

trait WithMultipleUsers {
  this: MyTwitterFeature with MyTwitterSteps ⇒

  beforeEachScenario (
    register_a_user(handle = "@test", name = "Test User", alias = "test-user"),
    register_a_user(handle = "@user1", name = "User One", alias = "user1"),
    register_a_user(handle = "@user2", name = "User Two", alias = "user2"),
    register_a_user(handle = "@user3", name = "User Three", alias = "user3")
  )

  afterEachScenario (
    delete_user("<test-user>"),
    delete_user("<user1>"),
    delete_user("<user2>"),
    delete_user("<user3>")
  )
}
