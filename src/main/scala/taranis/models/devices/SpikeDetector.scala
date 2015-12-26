package taranis.models.devices

import taranis.core.{Time, Forge}
import taranis.models.devices.SpikeDetector.withParams

import scala.collection.mutable

class SpikeDetector(params: withParams) {

  import params._

  val history = mutable.ListBuffer.empty[(Time, Double)]

}

object SpikeDetector {

  case class withParams() extends Forge[SpikeDetector]

  object default extends withParams

}