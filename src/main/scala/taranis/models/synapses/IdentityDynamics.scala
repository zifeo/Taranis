package taranis.models.synapses

import taranis.core.dynamics.EventDynamics
import taranis.core.events.Event
import taranis.core.{Forge, Time}
import taranis.models.synapses.IdentityDynamics.withParams

object IdentityDynamics {

  case class withParams() extends Forge[IdentityDynamics]

  object default extends withParams

}

final class IdentityDynamics(params: withParams) extends EventDynamics {

  override def update(time: Time): Unit = ()

  override def calibrate(resolution: Time): Unit = ()

  override val handle: PartialFunction[Event, Event] = {
    case e => e
  }

}
