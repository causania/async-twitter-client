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
  val akka_repo = "Akka Repo" at "http://akka.io/repository"
  val typesafe_repo = "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
  val web_plugin_repo = "Web plugin repo" at "http://siasia.github.com/maven2"
  val sbt_idea_repo = "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
  val all = Seq(akka_repo, typesafe_repo, web_plugin_repo, sbt_idea_repo, Classpaths.typesafeResolver)
}

object Dependencies {
  val akka_kernel = "se.scalablesolutions.akka" % "akka-kernel" % "1.3-RC1"
  val akka_sbt = "com.typesafe.akka" % "akka-sbt-plugin" % "2.0-SNAPSHOT" from "http://repo.typesafe.com/typesafe/internal-snapshots/com/typesafe/akka/akka-sbt-plugin_2.9.1_0.11.0/2.0-SNAPSHOT/akka-sbt-plugin-2.0-SNAPSHOT.jar"
  val all = Seq(akka_kernel, akka_sbt)
}
