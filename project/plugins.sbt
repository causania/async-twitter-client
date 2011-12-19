resolvers ++= Seq(
  "sbt-idea-repo" at "http://mpeltonen.github.com/maven/",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/internal-snapshots/"
)

// Plugins
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "0.11.0")

addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.0-SNAPSHOT"
  from "http://repo.typesafe.com/typesafe/internal-snapshots/com/typesafe/akka/akka-sbt-plugin_2.9.1_0.11.2/2.0-SNAPSHOT/akka-sbt-plugin-2.0-SNAPSHOT.jar")