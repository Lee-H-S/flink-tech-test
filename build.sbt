name := "flink-job"

version := "0.1"

scalaVersion := "2.12.15"

val flinkVersion = "1.14.6"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-clients" % flinkVersion,
  "io.spray" %% "spray-json" % "1.3.6",
  "org.apache.flink" %% "flink-scala" % flinkVersion,
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion,
  "org.apache.flink" %% "flink-connector-kafka" % flinkVersion,
  "com.typesafe" % "config" % "1.4.1"
)

val flinkConnectors = Seq(
  "org.apache.flink" % "flink-connector-files" % flinkVersion
)

val flinkTestDeps = Seq(
  "org.apache.flink" %% "flink-test-utils" % "1.14.6" % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
    "org.testcontainers" % "kafka" % "1.17.3" % Test,
    "org.apache.flink" %% "flink-test-utils" % "1.14.6" % Test,
  "io.github.embeddedkafka" %% "embedded-kafka" % "3.7.0" % Test,
  "org.apache.flink" %% "flink-test-utils" % "1.14.6" % Test,
  "org.scalatest" %% "scalatest" % "3.2.10" % Test
  )

libraryDependencies ++= flinkDependencies ++ flinkConnectors ++ flinkTestDeps

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}