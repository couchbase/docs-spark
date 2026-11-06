name := "docs"

version := "0.1"

scalaVersion := "2.13.18"

Compile / scalaSource := baseDirectory.value / "modules" / "ROOT" / "examples"
Compile / javaSource := baseDirectory.value / "modules" / "ROOT" / "examples"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "4.2.1",
  "org.apache.spark" %% "spark-sql" % "4.2.1",
  "com.couchbase.client" %% "spark-connector" % "4.0.3"
)

resolvers += Resolver.mavenLocal