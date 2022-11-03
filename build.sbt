import Dependencies._

name := "streams-playground"
version := "0.1"


lazy val playground = (project in file("."))
  .settings(CommonSettings())
  .settings(
    name := "playground",
    libraryDependencies ++= Seq(
      Libs.kafkaStreamsScala,
      Libs.kafkaStreamsAvro,
      Libs.avro4sKafka,
      Libs.scalaTest,
      Libs.kafkaStreamsTest
    ),
    dependencyOverrides += Libs.kafkaClient
  )

  