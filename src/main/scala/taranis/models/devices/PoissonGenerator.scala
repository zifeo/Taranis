package taranis.models.devices

import taranis.core.Parameters

class PoissonGenerator {

}

object PoissonGenerator {

  case class withParams(
    rate: Double
  ) extends Parameters(classOf[PoissonGenerator])

}