val scala3Version = "3.7.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "meal-recipe-finder",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit"               % "1.0.0" % Test,
      "org.typelevel" %% "cats-effect"         % "3.6.0",
      "org.http4s"    %% "http4s-ember-client" % "0.23.25",
      "org.http4s"    %% "http4s-circe"        % "0.23.25",
      "io.circe"      %% "circe-generic"       % "0.14.9",
      "io.circe"      %% "circe-parser"        % "0.14.9",
      "org.slf4j"      % "slf4j-nop"           % "2.0.17"
    ),

    initialCommands := s"""
      import cats.effect.unsafe.implicits.global
    """
  )
