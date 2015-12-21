package taranis.models.devices

import akka.actor.ActorRef
import taranis.Network
import taranis.events.Spike
import taranis.models.Node.Register
import taranis.models.devices.Multimeter.withRecorders
import taranis.models.{Node, Parameters}

import scala.collection.mutable

case class Multimeter[T](params: withRecorders[T]) extends Node {

  import Multimeter._
  import Network._
  import params._

  val records = recorders.toMap.map { case (label, _) =>
    label -> mutable.ListBuffer.empty[(Double, Double)]
  }

  override def receive: Receive = {

    case Register(node) =>
      recorders.foreach { case (label, extractor) =>
        node ! Recorder(label, extractor)
      }

    case Data(timestamp, label, value) =>
      //log.debug(s"incoming data (at $timestamp) $label: $value")
      records(label) += (timestamp.toDouble -> value)

    case Request(requester) =>
      val data = records.map { case (label, recorded) =>
        label -> recorded.toSeq
      }
      requester ! Results(data)

    case Calibrate(_) =>

    case Tick(_) =>
      sender ! AckTick

  }

  override def calibrate(resolution: Double): Unit = ()

  override def update(origin: Double): Unit = ()

  override def handle(e: Spike): Unit = ()
}

object Multimeter {

  type Extractor[T] = T => Double
  type Records = Map[String, Seq[(Double, Double)]]

  final case class Data(timestamp: Double, label: String, value: Double)

  final case class Recorder[T](label: String, extractor: Extractor[T])

  final case class Request(requester: ActorRef)

  final case class Results(data: Records)

  case class withRecorders[T](recorders: (String, Extractor[T])*) extends Parameters(classOf[Multimeter[T]])

}
