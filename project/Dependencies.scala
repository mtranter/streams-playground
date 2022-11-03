import sbt._
import Keys.scalaVersion

object Dependencies {

  object V {
    val kafka = "3.2.0"
    val kafkaAvro = "6.2.0"
    val avro4s = "4.0.10"
    val scalaTest = "3.2.12"
    val embeddedKafka = "3.2.0"
  }

  object Libs {
    val kafkaClient = "org.apache.kafka" % "kafka-clients" % V.kafka
    val kafkaStreams = "org.apache.kafka" % "kafka-streams" % V.kafka
    val kafkaStreamsScala = "org.apache.kafka" %% "kafka-streams-scala" % V.kafka
    val kafkaAvro = "io.confluent" % "kafka-avro-serializer" % V.kafkaAvro
    val kafkaStreamsAvro = "io.confluent" % "kafka-streams-avro-serde" % V.kafkaAvro

    val avro4sCore = "com.sksamuel.avro4s" %% "avro4s-core" % V.avro4s
    val avro4sKafka = "com.sksamuel.avro4s" %% "avro4s-kafka" % V.avro4s

    val kafkaStreamsTest = "org.apache.kafka" % "kafka-streams-test-utils" % V.kafka % Test
    val embeddedKafka = "io.github.embeddedkafka" %% "embedded-kafka-streams" % V.embeddedKafka % Test
    val scalaTest = "org.scalatest" %% "scalatest" % V.scalaTest % Test
  }
}
