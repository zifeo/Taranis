package taranis.models.devices

import taranis.core.{Time, Parameters}
import taranis.models.devices.SpikeDetector.withParams

import scala.collection.mutable

class SpikeDetector(params: withParams) {

  import params._

  val history = mutable.ListBuffer.empty[(Time, Double)]

}

object SpikeDetector {

  case class withParams() extends Parameters(classOf[SpikeDetector])

  object default extends withParams

}