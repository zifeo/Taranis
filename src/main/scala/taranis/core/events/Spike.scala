package taranis.core.events

import taranis.core.Time

final case class Spike(time: Time, var delay: Time, var weight: Double) extends Event

