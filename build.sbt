
name := "my-twitter-playground"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  Resolver.bintrayRepo("krasserm", "maven"))

libraryDependencies ++= Seq(
  "com.github.krasserm"  % "akka-persistence-kafka_2.11"         % "0.4",
  "com.typesafe.akka"   %% "akka-http"                           % akkaHttpVersion,
  "com.typesafe.akka"   %% "akka-http-spray-json"                % akkaHttpVersion,
  "com.typesafe.akka"   %% "akka-persistence"                    % akkaVersion,
  "org.sangria-graphql" %% "sangria"                             % sangriaVersion,
  "org.sangria-graphql" %% "sangria-spray-json"                  % sangriaVersion,
  "com.github.agourlay" %% "cornichon"                           % "0.11" % Test)

lazy val akkaVersion = "2.4.16"
lazy val akkaHttpVersion = "10.0.3"
lazy val sangriaVersion = "1.0.0"

fork in run := true
