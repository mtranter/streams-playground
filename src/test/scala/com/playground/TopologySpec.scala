package com.playground

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.apache.kafka.streams.scala.{StreamsBuilder => ScalaStreamsBuilder}
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

case class User(id: String, username: String)              extends KafkaValue
case class Goal(id: String, name: String, ownerId: String) extends KafkaValue
case class HackWrapper(goals: List[Goal]) extends KafkaValue {
  def withGoal(g: Goal) = this.copy(goals = this.goals :+ g)
}
case class UserWithGoals(user: User, goals: List[Goal]) extends KafkaValue

object Topics {
  val users          = "Users"
  val goals          = "Goals"
  val usersWithGoals = "UsersWithGoals"
}

class TopologySpec extends AnyWordSpec with UsersGoalsJoinSpec with Matchers with BeforeAndAfterEach {

  "Topology" should {

    val streamBuilder = new ScalaStreamsBuilder()
    val usersTable    = streamBuilder.table[String, User](Topics.users)
    val goalsStream   = streamBuilder.stream[String, Goal](Topics.goals)
    val goalsByUser = goalsStream
      .selectKey((k, g) => g.ownerId)
      .groupByKey
      .aggregate(HackWrapper(List()))((k, g, n) => n.withGoal(g))

    "join user and goals on inner join" when {
      usersTable
        .join(goalsByUser)((u, g) => UserWithGoals(u, g.goals))
        .toStream
        .to(Topics.usersWithGoals)

      "keys match" in withTopology(streamBuilder.build()) {
        (usersTopic, goalsTopic, usersWithGoalsTopic) =>
          val user = User("1", "Fred")
          val goals = List(
            Goal("g1", "Goal One", "1"),
            Goal("g2", "Goal Two", "1")
          )
          usersTopic.pipeInput(user.id, user)
          goalsTopic.pipeKeyValueList(
            goals.map(g => KeyValue.pair(g.id, g)).asJava
          )

          val output = usersWithGoalsTopic.readKeyValuesToList()
          output.asScala should contain(
            KeyValue.pair(user.id, UserWithGoals(user, goals))
          )
      }
    }

    "join user and goals on outer join" when {
      usersTable
        .outerJoin(goalsByUser)((u, g) =>
          UserWithGoals(u, Option(g).fold(List[Goal]())(_.goals))
        )
        .toStream
        .to(Topics.usersWithGoals)

      "keys match" in withTopology(streamBuilder.build()) {
        (usersTopic, goalsTopic, usersWithGoalsTopic) =>
          val user = User("1", "Fred")
          val goals = List(
            Goal("g1", "Goal One", "1"),
            Goal("g2", "Goal Two", "1")
          )
          usersTopic.pipeInput(user.id, user)
          goalsTopic.pipeKeyValueList(
            goals.map(g => KeyValue.pair(g.id, g)).asJava
          )

          val output = usersWithGoalsTopic.readKeyValuesToList()
          output.asScala should contain(
            KeyValue.pair(user.id, UserWithGoals(user, goals))
          )
      }
    }
  }
}
