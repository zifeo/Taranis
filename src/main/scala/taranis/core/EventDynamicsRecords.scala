package taranis.core
/*
import akka.actor.ActorRef
import taranis.core.Records.{BindRecord, DataRecord, Extractor}
import taranis.core.dynamics.Dynamics

import scala.collection.mutable

trait EventDynamicsRecords extends EventsHub with Records {
  self: Records =>

  self.records



  abstract override def update(time: Time): Unit = {
    super.update(time)

    recorders.foreach { case (recorder, (label, extractor)) =>
      recorder ! DataRecord(time, label, extractor(self))
    }
  }

}
*/