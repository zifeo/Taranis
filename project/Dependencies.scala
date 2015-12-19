import sbt._

object Dependencies {

  val meta = Seq(
    "com.typesafe" % "config" % "1.3.0",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "ch.qos.logback" % "logback-access" % "1.1.3",
    "net.logstash.logback" % "logstash-logback-encoder" % "4.5.1",
    "com.storm-enroute" % "scalameter_2.11" % "0.7"
  )

  val akka = {
    val version = "2.4.1"
    Seq(
      "com.typesafe.akka" %% "akka-actor" % version,
      "com.typesafe.akka" %% "akka-slf4j" % version,
      "com.typesafe.akka" %% "akka-remote" % version,
      "com.typesafe.akka" %% "akka-testkit" % version % "test"
    )
  }

  val visual = Seq(
    "org.scalanlp" %% "breeze-viz" % "0.11.2"
  )

  val tests = Seq(
    "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )

}