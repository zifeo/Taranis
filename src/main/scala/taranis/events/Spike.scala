package taranis.events

final case class Spike(
                        weight: Double = 1,
                        multiplicity: Int = 1,
                        delay: Long = 1,
                        port: Int = 1
                      )
