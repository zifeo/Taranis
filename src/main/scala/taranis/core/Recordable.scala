package taranis.core

import akka.actor.ActorRef
import taranis.core.Recordable.{BindRecord, DataRecord, Extractor}
import taranis.core.dynamics.Dynamics

import scala.collection.mutable

/** Mix-in for recording data out of [[Dynamics]]. */
trait Recordable extends Dynamics with Configurable { self: Dynamics =>

  private val recorders = mutable.ArrayBuffer.empty[(ActorRef, Extractor[Dynamics])]

  abstract override def receive: PartialFunction[Any, Unit] =
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
      recorder ! DataRecord(time, label, extractor(self))
    }
  }

}

/** [[Recordable]] companion. */
object Recordable {

  type Extractor[T] = (String, T => Double)

  type Records = Map[String, Seq[(Double, Double)]]

  /** Binds a recorder with the current element using given extractor. */
  final case class BindRecord[T](recorder: ActorRef, extractor: Extractor[T])

  /** Contains a labelled and timed records. */
  final case class DataRecord(timestamp: Time, label: String, value: Double)

}
