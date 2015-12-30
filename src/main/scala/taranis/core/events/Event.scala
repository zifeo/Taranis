package taranis.core.events

import taranis.core.Time

trait Event {

  val time: Time

  var delay: Time

  final val delivery: Time = time + delay

  final def informative: EventInfo = EventInfo(this)

}

