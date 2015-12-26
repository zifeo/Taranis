package taranis.models.devices

import akka.actor.{Actor, ActorLogging}
import taranis.core.Recordable.{DataRecord, Extractor}
import taranis.core.{Forge, Time}
import taranis.models.devices.Multimeter.{Request, withRecorders}

import scala.collection.mutable

object Multimeter {

  object Request

  case class withRecorders[T](recorders: Extractor[T]*) extends Forge[Multimeter[T]]

}

final class Multimeter[T](params: withRecorders[T]) extends Actor with ActorLogging {

  import params._

  val records = recorders.toMap.map { case (label, _) =>
    label -> mutable.ListBuffer.empty[(Time, Double)]
  }

  override def receive: Receive = {

    case DataRecord(time, label, value) =>
      records(label) += time -> value

    case Request =>
      val data = records.map { case (label, recorded) =>
        label -> recorded.toSeq
      }
      sender ! data

  }
}
