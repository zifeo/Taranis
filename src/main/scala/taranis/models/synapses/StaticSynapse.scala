package taranis.models.synapses

import taranis.core.dynamics.EventDynamics
import taranis.core.events.{Event, Spike}
import taranis.core.{Forge, Time}
import taranis.models.synapses.StaticSynapse.withParams

object StaticSynapse {

  case class withParams(weight: Double = 1, delay: Time = 1) extends Forge[StaticSynapse]

  object default extends withParams

}

final class StaticSynapse(params: withParams) extends EventDynamics {

  import params._

  override def update(time: Time): Unit = ()

  override def calibrate(resolution: Time): Unit = ()

  override val handle: PartialFunction[Event, Event] = {
    case e: Spike => e.copy(delay = delay, weight = e.weight * weight)
  }

}
