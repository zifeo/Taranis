package taranis.models.devices

import taranis.core.dynamics.Dynamics
import taranis.core.{Entity, Time, Forge}
import taranis.models.devices.SpikeDetector.withParams

import scala.collection.mutable

object SpikeDetector {

  case class withParams() extends Forge[SpikeDetector]

  object default extends withParams

}

final class SpikeDetector(params: withParams) extends Entity with Dynamics {

  import params._

  val history = mutable.ListBuffer.empty[(Time, Double)]

  override def calibrate(resolution: Time): Unit = ()

  override def update(time: Time): Unit = ()

}
