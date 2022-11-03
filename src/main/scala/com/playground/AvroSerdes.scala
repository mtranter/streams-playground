package com.playground

import com.sksamuel.avro4s.RecordFormat
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.serialization.Serdes

import scala.jdk.CollectionConverters._
import com.sksamuel.avro4s.kafka.GenericSerde
import com.sksamuel.avro4s.Encoder
import com.sksamuel.avro4s.Decoder
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient

trait KafkaKey
trait KafkaValue

object AvroSerdes {

  private val props = Map("schema.registry.url" -> "http://localhost:8081")

  implicit def recordFormat[T: Encoder: Decoder] = RecordFormat[T]

  implicit def serializerFromSerde[T](implicit serde: Serde[T]) =
    serde.serializer()
  implicit def deserializerFromSerde[T](implicit serde: Serde[T]) =
    serde.deserializer()

  implicit def stringKeySerde: Serde[String] = Serdes.stringSerde

  implicit def keySerde[K >: Null <: KafkaKey](implicit
      krf: RecordFormat[K],
      src: SchemaRegistryClient
  ): Serde[K] = {
    val avroKeySerde = new GenericAvroSerde(src)
    avroKeySerde.configure(props.asJava, true)
    avroKeySerde.forCaseClass[K]
  }

  implicit def serde[V >: Null <: KafkaValue](implicit
      vrf: RecordFormat[V],
      src: SchemaRegistryClient
  ): Serde[V] = {
    val avroValueSerde = new GenericAvroSerde(src)
    avroValueSerde.configure(props.asJava, false)
    avroValueSerde.forCaseClass[V]
  }

  implicit class CaseClassSerde(inner: Serde[GenericRecord]) {
    def forCaseClass[T >: Null](implicit rf: RecordFormat[T]): Serde[T] = {
      Serdes.fromFn(
        (topic, data) => inner.serializer().serialize(topic, rf.to(data)),
        (topic, bytes) =>
          Option(rf.from(inner.deserializer().deserialize(topic, bytes)))
      )
    }
  }
}
