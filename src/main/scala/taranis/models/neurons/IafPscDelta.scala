package taranis.models.neurons

import taranis.core.events.Spike
import taranis.core.{Parameters, Neuron, Time}
import taranis.models.neurons.IafPscDelta.withParams

import scala.math.exp

class IafPscDelta(params: withParams) extends Neuron {

  import params._

  require(Vreset < Vth, "Reset potential must be smaller than threshold.")
  require(Cm > 0, "Capacitance must be >0.")
  require(tRef >= 0, "Refractory time must not be negative.")
  require(tauM > 0, "Membrane time constant must be > 0.")

  /** States. */
  var y0 = 0d
  var y3 = 0d
  var r = 0
  var refrspikesbuffer = 0d

  /** Variables. */
  var P30 = 0d
  var P33 = 0d
  var refractoryCounts = 0
  var h = 0d

  def Vm: Double =
    y3 + EL

  def calibrate(resolution: Time): Unit = {
    h = resolution
    P33 = exp( -h / tauM )
    P30 = 1 / Cm * ( 1 - P33 ) * tauM
    refractoryCounts = (tRef / resolution).toInt
  }

  def update(time: Time): Unit = {

    if (r == 0) {
      y3 = P30 * ( y0 + Ie ) + P33 * y3

      if (withRefrInput && refrspikesbuffer != 0.0) {
        y3 += refrspikesbuffer
        refrspikesbuffer = 0.0
      }

      y3 = if (y3 < Vmin) Vmin else y3
    } else {

      if (withRefrInput)
        refrspikesbuffer += bufferedSpike(time) * exp(r) * P33

      r -= 1
    }

    if (y3 >= Vth) {
      r = refractoryCounts
      y3 = Vreset
      send(Spike(time = time, delay = 1, weight = 1))
    }
  }

}

object IafPscDelta {

  /**
    *
    * @param tauM Membrane time constant in ms.
    * @param Cm Membrane capacitance in pF.
    * @param tRef Refractory period in ms.
    * @param EL Resting potential in m
    * @param Ie External DC current
    * @param Vth Threshold, the real threshold is (U0+Vth).
    * @param Vmin Lower bound, the real lower bound is (Vmin+Vth).
    * @param Vreset reset value of the membrane potential
    * @param withRefrInput spikes arriving during refractory period are counted
    */
  case class withParams(
                         tauM: Double = 10,
                         Cm: Double = 250,
                         tRef: Double = 2,
                         EL: Double = -70,
                         Ie: Double = 0,
                         Vth: Double = 15,
                         Vmin: Double = Double.MinValue,
                         Vreset: Double = 0,
                         withRefrInput: Boolean = false
                       ) extends Parameters(classOf[IafPscDelta])

}