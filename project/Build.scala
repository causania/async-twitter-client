import sbt._
import sbt.Keys._
import akka.sbt.AkkaKernelPlugin

object ServicesBuild extends Build {

  import BuildSettings._
  
  lazy val services = Project("services", file("."),
    settings = Defaults.defaultSettings ++ AkkaKernelPlugin.distSettings)
}

object BuildSettings {
  val buildOrganization = "org.koderama"
  val buildScalaVersion = "2.9.1"
  val buildSettings = Defaults.defaultSettings ++ Seq(
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    organization := buildOrganization,
    scalaVersion := buildScalaVersion,
    resolvers ++= Resolvers.all,
    libraryDependencies ++= Dependencies.all
  )
}

object Resolvers {
  val typesafe_releases = "Typesafe Release" at "http://repo.typesafe.com/typesafe/releases/"
  val typesafe_snapshots = "Typesafe Snaphots" at "http://repo.typesafe.com/typesafe/internal-snapshots/"
  val web_plugin_repo = "Web plugin repo" at "http://siasia.github.com/maven2"
  val sbt_idea_repo = "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
  val all = Seq(typesafe_snapshots, typesafe_releases, web_plugin_repo, sbt_idea_repo)
}

object Dependencies {
  val akka_kernel = "com.typesafe.akka" % "akka-kernel" % "2.0-SNAPSHOT"
  val akka_sbt = "com.typesafe.akka" % "akka-sbt-plugin" % "2.0-SNAPSHOT" from "http://repo.typesafe.com/typesafe/internal-snapshots/com/typesafe/akka/akka-sbt-plugin_2.9.1_0.11.2/2.0-SNAPSHOT/akka-sbt-plugin-2.0-SNAPSHOT.jar"
  val all = Seq(akka_kernel, akka_sbt)
}
