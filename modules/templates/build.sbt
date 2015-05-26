scalaVersion := "2.10.4"

name := "templates"

version := "1.0"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.10.4"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)