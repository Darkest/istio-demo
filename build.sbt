import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.dockerUsername

name := "istio-demo"
version := "0.1"
scalaVersion := "2.13.2"


lazy val global = project
  .in(file("."))
  .settings(commonSettings)
  .aggregate(
    client,
    server
  )

lazy val client = project
  .settings(
    name := "client",
    commonSettings ++ dockerCommonSettings,
    libraryDependencies ++= dependencies
  ).enablePlugins(DockerPlugin, JavaAppPackaging)

lazy val server = project
  .settings(
    name := "server",
    commonSettings ++ dockerCommonSettings ++ dockerServerSettings,
    libraryDependencies ++= dependencies
  ).enablePlugins(DockerPlugin, JavaAppPackaging)

val circeVersion = "0.12.3"

val dependencies = Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.12",
  "com.typesafe.akka" %% "akka-stream" % "2.6.6",
  "de.heikoseeberger" %% "akka-http-circe" % "1.31.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.6",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)


lazy val compilerOptions = Seq(
  "-Xcheckinit",
  //"-Xfatal-warnings",
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
)

lazy val dockerCommonSettings = Seq(
  dockerExposedPorts := Seq(8080),
  dockerUpdateLatest := true,
  dockerUsername:= Some("d4rkest")
)

lazy val dockerServerSettings = Seq(
  dockerAliases ++= Seq(dockerAlias.value.withUsername(Some("d4rkest")).withName("istio-demo-server")),
)