package taranis.models.synapses

import taranis.core.events.Spike
import taranis.core.{Forge, Synapse, Time}
import taranis.models.synapses.StaticSynapse.withParams

case class StaticSynapse(params: withParams) extends Synapse {

  import params._

  def calibrate(resolution: Time): Unit = ()

  def update(time: Time): Unit = ()

  def handle(spike: Spike): Unit = {
    send(spike)
  }

}

object StaticSynapse {

  case class withParams(weight: Double, delay: Time) extends Forge[StaticSynapse]

}