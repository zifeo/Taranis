package taranis.models.devices

import akka.actor.{Actor, ActorLogging}
import taranis.core.Node.Register
import taranis.core.{Parameters, Time}
import taranis.models.devices.Multimeter.withRecorders

import scala.collection.mutable

case class Multimeter[T](params: withRecorders[T]) extends Actor with ActorLogging {

  import Multimeter._
  import params._

  val records = recorders.toMap.map { case (label, _) =>
    label -> mutable.ListBuffer.empty[(Time, Double)]
  }

  override def receive: Receive = {

    case Register(node) =>
      log.debug(s"register: $node")
      recorders.foreach { recorder =>
        node ! Record(recorder)
      }

    case Data(time, label, value) =>
      records(label) += time -> value

    case Request =>
      val data = records.map { case (label, recorded) =>
        label -> recorded.toSeq
      }
      sender ! data

  }
}

object Multimeter {

  type Extractor[T] = (String, T => Double)

  type Records = Map[String, Seq[(Double, Double)]]

  final case class Data(timestamp: Double, label: String, value: Double)

  final case class Record[T](extractor: Extractor[T])

  object Request

  case class withRecorders[T](recorders: Extractor[T]*) extends Parameters(classOf[Multimeter[T]])

}
