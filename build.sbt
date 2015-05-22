scalaVersion := "2.11.5"

name := "template-macro"

version := "1.0"

lazy val root = (project in file(".")).aggregate(macros, templates).dependsOn(templates)

lazy val macros = project.in(file("modules/macros"))

lazy val templates = project.in(file("modules/templates")).dependsOn(macros)
