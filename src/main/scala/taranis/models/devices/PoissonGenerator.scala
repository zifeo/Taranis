package taranis.models.devices

import breeze.stats.distributions.Poisson
import taranis.core.events.Spike
import taranis.core.{Neuron, Time, Parameters}
import taranis.models.devices.PoissonGenerator.withParams

class PoissonGenerator(params: withParams) extends Neuron {

  import params._

  require(rate > 0, "The rate cannot be negative.")

  var poisson: Poisson = _

  def calibrate(resolution: Time): Unit = {
    poisson = Poisson(resolution * 1e-3 * rate)
  }

  def update(time: Time): Unit = {
    if (rate > 0) {

      val spikeCount = poisson.sample()
      if (spikeCount > 0) {

        val spike = Spike(time = time, delay = 1, weight = 1)
        var i = 0
        while (i < spikeCount) {
          send(spike)
          i += 1
        }
      }
    }
  }

}

object PoissonGenerator {

  /**
    *
    * @param rate process rate in Hz
    * @param start begin of device application with resp. to origin in ms
    * @param stop end of device application with resp. to origin in ms
    */
  case class withParams(
                         rate: Double = 0,
                         start: Time = 0,
                         stop: Time = Double.MaxValue
                       ) extends Parameters(classOf[PoissonGenerator])

}