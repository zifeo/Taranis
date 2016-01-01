package taranis.examples

import breeze.plot._
import taranis.dsl._
import taranis.models.devices._
import taranis.models.neurons._
import taranis.models.synapses._

import scala.concurrent.duration._
import scala.language.postfixOps

object TwoNeurons extends App {

  val weight = 20.0
  val delay = 1
  val stim = 1000.0

  val neuron1 = create(IafNeuron.withParams(Ie = stim))
  val neuron2 = create(IafNeuron.default)
  val multimeter = create(Multimeter.withRecorders[IafNeuron](
    "Vm" -> (_.Vm)
  ))

  connect(neuron1, neuron2, StaticSynapse.withParams(weight = weight, delay = delay))
  connectDevice(multimeter, neuron2)

  simulate(100 milliseconds, 1 millisecond)

  val (xs, ys) = data(multimeter)("Vm").unzip

  val p = Figure().subplot(0)
  p.xlabel = "Time (ms)"
  p.ylabel = "Membrane potential (mV)"
  p.legend = true
  p.title = "Membrane potential"
  p.xlim = (0, 100)
  p.ylim = (-70, -69.3)
  p.setYAxisDecimalTickUnits()
  p += plot(xs, ys, name = "Neuron 2")

  //noGUI(p)
  terminate()

}
