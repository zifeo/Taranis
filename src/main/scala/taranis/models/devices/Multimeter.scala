package taranis.models.devices

import akka.actor.ActorRef
import taranis.core.Records.{BindRecord, DataRecord, Extractor}
import taranis.core.dynamics.Dynamics
import taranis.core.{Entity, Forge, Time}
import taranis.models.devices.Multimeter.{BindRecorder, Metrics, withRecorders}

import scala.collection.mutable

object Multimeter {

  object Metrics

  case class withRecorders[T](recorders: Extractor[T]*) extends Forge[Multimeter[T]]

  case class BindRecorder(node: ActorRef)

}

final class Multimeter[T](params: withRecorders[T]) extends Entity with Dynamics {

  import params._

  val records = mutable.AnyRefMap(recorders: _*).map { case (label, _) =>
    label -> mutable.ArrayBuffer.empty[(Time, Double)]
  }

  override def receive: Receive =
    super.receive.orElse {

      case BindRecorder(node) =>
        recorders.foreach { extractor =>
          node ! BindRecord(self, extractor)
        }
        log.debug(s"register: $node")

      case DataRecord(time, label, value) =>
        records(label) += time -> value

      case Metrics =>
        val data = records.map { case (label, recorded) =>
          label -> recorded.toList
        }.toMap
        sender ! data

    }

  override def calibrate(resolution: Time): Unit = ()

  override def update(time: Time): Unit = ()

}
