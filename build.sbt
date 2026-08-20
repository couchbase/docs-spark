name := "docs"

version := "0.1"

scalaVersion := "2.13.18"

Compile / scalaSource := baseDirectory.value / "modules" / "ROOT" / "examples"
Compile / javaSource := baseDirectory.value / "modules" / "ROOT" / "examples"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "4.2.0",
  "org.apache.spark" %% "spark-sql" % "4.2.0",
  "com.couchbase.client" %% "spark-connector" % "4.0.0"
)

resolvers += Resolver.mavenLocal