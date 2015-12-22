package taranis.models.synapses

import taranis.events.Spike
import taranis.models.Parameters
import taranis.models.synapses.StaticSynapse.withParams

case class StaticSynapse(params: withParams) extends Synapse {

  import params._

  def bridged(e: Spike): Spike = {
    e.weight = weight
    e.delay = delay
    e
  }

}

object StaticSynapse {

  case class withParams(weight: Double, delay: Long) extends Parameters(classOf[StaticSynapse])

}