package taranis.core

import akka.actor.ActorRef
import taranis.core.Records.{BindRecord, DataRecord, Extractor}
import taranis.core.dynamics.Dynamics

import scala.collection.mutable

/** Mix-in for recording data out of [[Dynamics]]. */
trait Records extends Entity {

  private val recorders = mutable.ArrayBuffer.empty[(ActorRef, Extractor[Dynamics])]

  abstract override def receive: Receive =
    super
      .receive
      .orElse { case BindRecord(recorder, extractor) =>
        recorders += recorder -> extractor
        val label = extractor._1
        log.debug(s"record: $label")
      }

  abstract override def update(time: Time): Unit = {
    super.update(time)

    recorders.foreach { case (recorder, (label, extractor)) =>
      recorder ! DataRecord(time, label, extractor(this))
    }
  }

}

/** [[Records]] companion. */
object Records {

  type Extractor[T] = (String, T => Double)

  type RecordedData = Map[String, Seq[(Double, Double)]]

  /** Binds a recorder with the current element using given extractor. */
  final case class BindRecord[T](recorder: ActorRef, extractor: Extractor[T])

  /** Contains a labelled and timed records. */
  final case class DataRecord(timestamp: Time, label: String, value: Double)

}
