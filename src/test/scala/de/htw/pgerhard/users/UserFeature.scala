package de.htw.pgerhard.users

import com.github.agourlay.cornichon.core.FeatureDef
import com.github.agourlay.cornichon.json.CornichonJson._
import de.htw.pgerhard.MyTwitterFeature
import sangria.macros._

class UserFeature extends MyTwitterFeature {
  override def feature: FeatureDef = Feature("User Feature") {

    Scenario("Register a user") {

      When I query_gql("/graphql").withQuery(
        graphql"""
          mutation {
            registerUser(handle: "@pgerhard", name: "Peter Gerhard") {
              id
            }
          }
        """)

      Then assert body.path("data").isPresent

      And I save_body_path("data.registerUser.id" → "peter-id")

      When I query_gql("/graphql").withQuery(
        graphql"""
          query {
            user(userId: "<peter-id>") {
              id
              handle
              name
              following { id },
              followers { id }
            }
          }
        """)

      Then assert body.path("data.user").is(
        gql"""
          {
            id: "<peter-id>",
            handle: "@pgerhard",
            name: "Peter Gerhard",
            following: [],
            followers: []
          }
        """)
    }

    Scenario("Set a users name") {

      When I query_gql("/graphql").withQuery(
        graphql"""
          mutation {
            registerUser(handle: "@pgerhard", name: "Peter Gerhard") {
              id
            }
          }
        """)

      Then assert body.path("data").isPresent

      And I save_body_path("data.registerUser.id" → "peter-id")

      When I query_gql("/graphql").withQuery(
        graphql"""
          mutation {
            setUserName(userId: "<peter-id>", name: "Peter X") {
              id
              name
            }
          }
        """)

      Then assert body.path("data.setUserName").is(
        gql"""
          {
            id: "<peter-id>",
            name: "Peter X"
          }
        """)
    }

    // Todo unfollow
    Scenario("Follow and unfollow") {
      When I query_gql("/graphql").withQuery(
        graphql"""
          mutation {
            registerUser(handle: "@pgerhard", name: "Peter Gerhard") {
              id
            }
          }
        """)

      And I save_body_path("data.registerUser.id" → "peter-id")

      When I query_gql("/graphql").withQuery(
        graphql"""
          mutation {
            registerUser(handle: "@hwurst", name: "Hans Wurst") {
              id
            }
          }
        """)

      And I save_body_path("data.registerUser.id" → "hans-id")

      When I query_gql("/graphql").withQuery(
        graphql"""
          mutation {
            followUser(userId: "<peter-id>", followingId: "<hans-id>") {
              id
            }
          }
        """)

      When I query_gql("/graphql").withQuery(
        graphql"""
          mutation {
            followUser(userId: "<hans-id>", followingId: "<peter-id>") {
              id
            }
          }
        """)

      When I query_gql("/graphql").withQuery(
        graphql"""
          query {
            user(userId: "<peter-id>") {
              following { id }
              followers { id }
            }
          }
        """)

      Then assert body.path("data.user").is(
        gql"""
          {
            following: [{
              id: "<hans-id>"
            }]
            followers: [{
              id: "<hans-id>"
            }]
          }
        """)
    }

    Scenario("Delete a User") {

      When I query_gql("/graphql").withQuery(
        graphql"""
          mutation {
            registerUser(handle: "@pgerhard", name: "Peter Gerhard") {
              id
            }
          }
        """)

      Then assert body.path("data").isPresent

      And I save_body_path("data.registerUser.id" → "peter-id")

      When I query_gql("/graphql").withQuery(
        graphql"""
          mutation {
            deleteUser(userId: "<peter-id>") {
              id
            }
          }
        """)

      Then assert body.path("data").is(
        gql"""
          {
            deleteUser: null
          }
        """)
    }
  }
}
