package taranis.core.events

import taranis.core.Time

final case class Spike(time: Time, delay: Time, weight: Double) extends Event

