package com.playground

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.apache.kafka.streams.scala.{StreamsBuilder => ScalaScreamsBuilder}
import com.playground.AvroSerdes._
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.common.serialization._
import org.apache.kafka.streams.TopologyTestDriver
import java.util.Properties
import org.apache.kafka.streams.StreamsConfig
import scala.jdk.CollectionConverters._
import org.apache.kafka.streams.KafkaStreams
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import org.scalatest.BeforeAndAfterEach
import org.apache.kafka.streams.test.TestRecord
import org.apache.kafka.streams._

trait UsersGoalsJoinSpec {

  implicit val schemaRegistryClient: SchemaRegistryClient =
    new MockSchemaRegistryClient()

  def withTopology(topology: Topology)(
      testCode: (
          TestInputTopic[String, User],
          TestInputTopic[String, Goal],
          TestOutputTopic[String, UserWithGoals]
      ) => Any
  ) = {

    val config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test")
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234")

    val testDriver = new TopologyTestDriver(topology, config)
    val usersTopic = testDriver.createInputTopic(
      Topics.users,
      implicitly[Serializer[String]],
      implicitly[Serializer[User]]
    )
    val goalsTopic = testDriver.createInputTopic(
      Topics.goals,
      implicitly[Serializer[String]],
      implicitly[Serializer[Goal]]
    )
    val usersWithGoalsTopic = testDriver.createOutputTopic(
      Topics.usersWithGoals,
      implicitly[Deserializer[String]],
      implicitly[Deserializer[UserWithGoals]]
    )
    try {
      testCode(usersTopic, goalsTopic, usersWithGoalsTopic)
    } finally testDriver.close()
  }
}
