package taranis.events

final case class Spike(
                        var weight: Double = 1,
                        var multiplicity: Int = 1,
                        var delay: Long = 1,
                        var port: Int = 1
                      )
