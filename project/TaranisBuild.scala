import com.typesafe.sbt.SbtAspectj._
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._

object TaranisBuild extends Build {

  lazy val taranis = project
    .in(file("."))
    .settings(aspectjSettings: _ *)
    .settings(

      name := "Taranis",
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

      fork := true,
      autoAPIMappings := true,
      cancelable in Global := true,
      parallelExecution in Test := true,
      evictionWarningOptions in update := EvictionWarningOptions.empty,

      assemblyJarName in assembly := "taranis.jar",
      test in assembly := {},
      mainClass in assembly := Some("taranis.examples.Brunnel"),

      target in (Compile, doc) := baseDirectory.value / "api",

      javaOptions += "-Xmx4G"
      //,javaOptions in run <++= AspectjKeys.weaverOptions in Aspectj

    )

}