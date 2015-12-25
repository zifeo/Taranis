package taranis.core.events

import taranis.core.Time

final case class Spike(
                        time: Time,
                        var delay: Time,
                        var weight: Double,
                        var port: Int = 1
                      )