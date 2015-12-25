package taranis.models.devices

import taranis.core.Parameters

class SpikeDetector {

}

object SpikeDetector {

  case class withParams() extends Parameters(classOf[SpikeDetector])

  case object default extends withParams

}