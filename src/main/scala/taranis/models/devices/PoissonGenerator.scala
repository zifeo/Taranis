package taranis.models.devices

import breeze.stats.distributions.Poisson
import taranis.core.events.Spike
import taranis.core.{Forge, Neuron, Time}
import taranis.models.devices.PoissonGenerator.withParams

object PoissonGenerator {

  /** Rewritten from NEST under GNU General Public License 2 or later:
    * http://www.nest-simulator.org
    *
    * @param rate process rate in Hz
    * @param start begin of device application with resp. to origin in ms
    * @param stop end of device application with resp. to origin in ms
    */
  case class withParams(
                         rate: Double = 0,
                         start: Time = 0,
                         stop: Time = Double.MaxValue
                       ) extends Forge[PoissonGenerator]

}

final class PoissonGenerator(params: withParams) extends Neuron {

  import params._

  require(rate > 0, "The rate cannot be negative nor null.")
  require(start >= 0, "The start cannot be negative.")
  require(stop > start, "The stop cannot be before the start.")

  var poisson: Poisson = _

  override def calibrate(resolution: Time): Unit = {
    poisson = Poisson(resolution * 1e-3 * rate)
  }

  override def update(time: Time): Unit = {
    if (rate > 0 && time >= start && time < stop) {
      successors.foreach { case (successor, dynamic) =>
        val spikeCount = poisson.sample()
        if (spikeCount > 0)
          successor ! dynamic.handle(Spike(time = time, delay = 1, weight = spikeCount))
      }
    }
  }

}
