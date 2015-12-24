import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._

object TaranisBuild extends Build {

  lazy val taranis = (project in file(".")).settings(

    name := "Yields-server",
    organization := "taranis",
    version := "0.1.0",

    scalaVersion := "2.11.7",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint:_"
    ),

    libraryDependencies ++= {
      import Dependencies._
      meta ++ akka ++ visual ++ kamon ++ tests
    },

    scalacOptions in (Compile, doc) ++= Seq(
      "-groups",
      "-implicits",
      "-no-link-warnings"
    ),
    javacOptions in (Compile, doc) ++= Seq(
      "-notimestamp",
      "-linksource"
    ),

    cancelable in Global := true,
    fork := true,
    autoAPIMappings := true,
    parallelExecution in Test := false,
    evictionWarningOptions in update := EvictionWarningOptions.empty,

    assemblyJarName in assembly := "yields.jar",
    test in assembly := {},
    mainClass in assembly := Some("yields.server.Yields"),

    target in (Compile, doc) := baseDirectory.value / "api"

  )

}