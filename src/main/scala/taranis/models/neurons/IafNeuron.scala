package taranis.models.neurons

import breeze.numerics.expm1
import taranis.events.Spike
import taranis.models.Parameters
import taranis.models.neurons.IafNeuron.withParams

import scala.math.{abs, exp}

class IafNeuron(params: withParams) extends Neuron {

  import params._

  require(VReset < theta, "reset potential must be smaller than threshold")
  require(C > 0, "capacitance must be strictly positive")
  require(tau > 0 && tauSyn > 0 && tauR > 0, "all time constants must be strictly positive")

  /** States. */
  // Constant current
  var y0 = 0d
  var y1 = 0d
  var y2 = 0d
  // This is the membrane potential RELATIVE TO RESTING POTENTIAL
  var y3 = 0d
  var r = 0

  /** Variables. */
  var PSCInitialValue = 0d
  // refractory time in steps
  var P11 = 0d
  var P21 = 0d
  var P22 = 0d
  var P31 = 0d
  var P32 = 0d
  var P30 = 0d
  var P33 = 0d
  var refractoryCounts = 0

  def Vm: Double =
    y3 + U0

  override def calibrate(resolution: Double): Unit = {
    P11 = exp(- resolution / tauSyn)
    P22 = P11
    P33 = exp(- resolution / tau)
    P21 = resolution * P11
    P30 = 1 / C * ( 1 - P33 ) * tau
    P31 = propagator_31( tauSyn, tau, C, resolution )
    P32 = propagator_32( tauSyn, tau, C, resolution )
    PSCInitialValue = 1.0 * math.E / tauSyn
    refractoryCounts = tauR.toInt
  }

  override def update(origin: Double): Unit = {

    if (r == 0) {
      // neuron not refractory
      y3 = P30 * ( y0 + Ie ) + P31 * y1 + P32 * y2 + P33 * y3
    } else {
      // neuron is absolute refractory
      r -= 1
    }

    y2 = P21 * y1 + P22 * y2
    y1 *= P11
    y1 += PSCInitialValue * spikesValue

    if ( y3 >= theta ) {
      y3 = VReset
      //r = refractoryCounts

      send(Spike())
    }

    records(this, origin)
  }

  def propagator_31(tau_syn: Double, tau: Double, C: Double, h: Double): Double = {
    val P31_linear = 1 / ( 3d * C * tau * tau ) * h * h * h * ( tau_syn - tau ) * exp( -h / tau )
    val P31 = 1 / C * ( exp( -h / tau_syn ) * expm1( -h / tau + h / tau_syn ) / ( tau / tau_syn - 1 ) * tau - h * exp( -h / tau_syn ) ) / ( -1 - -tau / tau_syn ) * tau
    val P31_singular = h * h / 2 / C * exp( -h / tau )
    val dev_P31 = abs( P31 - P31_singular )

    if ( tau == tau_syn || ( math.abs( tau - tau_syn ) < 0.1 && dev_P31 > 2 * abs( P31_linear ) ) )
      P31_singular
    else
      P31
  }

  def propagator_32(tau_syn: Double, tau: Double, C: Double, h: Double): Double = {
    val P32_linear = 1 / ( 2d * C * tau * tau ) * h * h * ( tau_syn - tau ) * exp( -h / tau )
    val P32_singular = h / C * exp( -h / tau )
    val P32 = -tau / ( C * ( 1 - tau / tau_syn ) ) * exp( -h / tau_syn ) * expm1( h * ( 1 / tau_syn - 1 / tau ) )
    val dev_P32 = abs( P32 - P32_singular )

    if ( tau == tau_syn ||( abs( tau - tau_syn ) < 0.1 && dev_P32 > 2 * abs( P32_linear ) ) )
      P32_singular
    else
      P32
  }

}

object IafNeuron {

  /**
    * @param C membrane capacitance in pF
    * @param tau membrane time constant in ms
    * @param tauSyn time constant of synaptic current in ms
    * @param tauR refractory period in ms
    * @param U0 resting potential in mV
    * @param VReset reset value of the membrane potential in mV
    * @param theta threshold in mV
    * @param Ie external current in pA
    */
  case class withParams(
                         C: Double = 250,
                         tau: Double = 10,
                         tauSyn: Double = 2,
                         tauR: Double = 2,
                         U0: Double = -70,
                         VReset: Double = 0,
                         theta: Double = 15,
                         Ie: Double = 0
                       ) extends Parameters(classOf[IafNeuron])

  object default extends withParams

}