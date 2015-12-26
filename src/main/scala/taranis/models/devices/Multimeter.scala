package taranis.models.devices

import akka.actor.{Actor, ActorLogging}
import taranis.core.Entity.Register
import taranis.core.Recordable.{DataRecord, Extractor, BindRecord}
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
        node ! BindRecord(recorder)
      }

    case DataRecord(time, label, value) =>
      records(label) += time -> value

    case Request =>
      val data = records.map { case (label, recorded) =>
        label -> recorded.toSeq
      }
      sender ! data

  }
}

object Multimeter {

  object Request

  case class withRecorders[T](recorders: Extractor[T]*) extends Parameters(classOf[Multimeter[T]])

}
