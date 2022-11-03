import sbt._
import Keys._

object CommonSettings {
  val scalaV = "2.13.6"
  def apply() =
    Seq(
      scalaVersion := scalaV,
      resolvers += "Confluent Maven Repository" at "https://packages.confluent.io/maven/"
    )
}
