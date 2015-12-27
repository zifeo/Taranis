package taranis.core.dynamics

import taranis.core.events.Event

trait EventDynamics extends Dynamics {

  val handle: PartialFunction[Event, Event]

}
