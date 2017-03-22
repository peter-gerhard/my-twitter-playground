name := "my-twitter-playground"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions := Seq(
  "-target:jvm-1.8",
  "-unchecked",
  "-deprecation",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Ywarn-unused")

libraryDependencies ++= Seq(
  "com.typesafe.akka"          %% "akka-http"                           % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-http-spray-json"                % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-persistence"                    % akkaVersion,
  "com.typesafe.akka"          %% "akka-persistence-query-experimental" % akkaVersion,
  "de.heikoseeberger"          %% "akka-sse"                            % "2.0.0",
  "org.iq80.leveldb"            % "leveldb"                             % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"                      % "1.8",
  "org.sangria-graphql"        %% "sangria"                             % sangriaVersion,
  "org.sangria-graphql"        %% "sangria-akka-streams"                % sangriaVersion,
  "org.sangria-graphql"        %% "sangria-spray-json"                  % sangriaVersion,
  "com.github.agourlay"        %% "cornichon"                           % "0.11" % Test)

lazy val akkaVersion = "2.4.16"
lazy val akkaHttpVersion = "10.0.3"
lazy val sangriaVersion = "1.0.0"

fork := true
