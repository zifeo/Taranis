package taranis.models.synapses

import taranis.core.events.Spike
import taranis.core.{Parameters, Synapse}
import taranis.models.synapses.StaticSynapse.withParams

case class StaticSynapse(params: withParams) extends Synapse {

  import params._

  def bridged(spike: Spike): Spike = {
    spike.weight = weight
    spike.delay = delay
    spike
  }

}

object StaticSynapse {

  case class withParams(weight: Double, delay: Long) extends Parameters(classOf[StaticSynapse])

}