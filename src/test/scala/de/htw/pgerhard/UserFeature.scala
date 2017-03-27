package de.htw.pgerhard

import com.github.agourlay.cornichon.core.FeatureDef

class UserFeature extends MyTwitterFeature with MyTwitterSteps {

  override def feature: FeatureDef = Feature("User Feature") {

    Scenario("Register a user") {

      When I register_a_user(handle = "@pgerhard", name = "Peter Gerhard", alias = "peter")

      eventually {
        When I query_user("<peter>")

        Then assert body.path("data.user").is(
          """
        {
          "id": "<peter-id>",
          "handle": "@pgerhard",
          "name": "Peter Gerhard",
          "subscriptions": [],
          "subscribers": []
        }
        """)
      }

      And afterwards delete_user("<peter>")
    }

    Scenario("Set a users name") {

      When I register_a_user(handle = "@pgerhard", name = "Peter Gerhard", alias = "peter")

      And the user("<peter>").setsName("Peter Fox")

      Then assert body.path("data.setUserName.name").is("Peter Fox")

      And afterwards delete_user("<peter>")
    }

    Scenario("Follow and unfollow a user") {

      When I register_a_user(handle = "@pgerhard", name = "Peter Gerhard", alias = "peter")

      And I register_a_user(handle = "@hwurst", name = "Hans Wurst", alias = "hans")

      And the user("<peter>").follows("<hans>")

      Then assert body.path("data.addSubscription.subscriptions").is("""[{ "id": "<hans>" }]""")

      eventually {
        When I query_user("<hans>")

        Then assert body.path("data.user.subscribers").is("""[{ "id": "<peter>" }]""")
      }


      When the user("<peter>").unfollows("<hans>")

      Then assert body.path("data.removeSubscription.subscriptions").asArray.isEmpty

      eventually {
        When I query_user("<hans>")

        Then assert body.path("data.user.subscribers").asArray.isEmpty
      }

      And afterwards delete_user("<peter>")

      And afterwards delete_user("<hans>")
    }

    Scenario("Delete a User") {

      When I register_a_user(handle = "@pgerhard", name = "Peter Gerhard", alias = "peter")

      Then assert body.path("data.registerUser").isPresent

      When I delete_user("<peter>")

      Then assert body.path("data.deleteUser").is(true)
    }
  }
}
