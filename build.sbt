organization := "org.koderama"

name := "async-twitter-client"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.9.1"

scalacOptions ++= Seq("-deprecation", "-unchecked")

// Compile
libraryDependencies ++= {
  Seq(
    "ch.qos.logback" % "logback-classic" % "1.0.0",
    "org.codehaus.jackson" % "jackson-core-asl" % "1.9.2" withSources(),
    "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.2" withSources(),
    "com.fasterxml" % "jackson-module-scala" % "1.9.1-SNAPSHOT" withSources(),
    "joda-time" % "joda-time" % "2.0",
    "org.joda" % "joda-convert" % "1.2",
    "org.scribe" % "scribe" % "1.2.3" withSources(),
    "com.ning" % "async-http-client" % "1.7.0-SNAPSHOT" withSources(),
    "se.scalablesolutions.akka" % "akka-actor" % "1.3-RC1" withSources(),
    "se.scalablesolutions.akka" % "akka-stm" % "1.3-RC1" withSources()
  )
}

// Provided
libraryDependencies ++= {
  Seq(
  )
}

// Test
libraryDependencies ++= {
  Seq(
    "org.specs2" %% "specs2" % "1.6.1" withSources(),
    "org.specs2" %% "specs2-scalaz-core" % "6.0.1" % "test" withSources(),
    "org.mockito" % "mockito-all" % "1.9.0-rc1" % "test" withSources(),
    "se.scalablesolutions.akka" % "akka-testkit" % "1.3-RC1" withSources()
  )
}

// Distribution
libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-sbt-plugin" % "2.0-SNAPSHOT"
    from "http://repo.typesafe.com/typesafe/internal-snapshots/com/typesafe/akka/akka-sbt-plugin_2.9.1_0.11.0/2.0-SNAPSHOT/akka-sbt-plugin-2.0-SNAPSHOT.jar",
  "se.scalablesolutions.akka" % "akka-kernel" % "1.3-RC1" withSources()
)


resolvers ++= Seq(
  "snapshots" at "http://scala-tools.org/repo-snapshots",
  "releases" at "http://scala-tools.org/repo-releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Coda Hale's Repository" at "http://repo.codahale.com/",
  "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"
)
