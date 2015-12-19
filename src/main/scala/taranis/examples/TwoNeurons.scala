package taranis.examples

import breeze.plot._
import taranis.Simulation
import taranis.models.devices.Multimeter
import taranis.models.neurons.IafNeuron
import taranis.models.synapses.StaticSynapse

import scala.concurrent.duration._
import scala.language.postfixOps

object TwoNeurons extends Simulation {

  val weight = 20.0
  val delay = 1
  val stim = 1000.0

  val neuron1 = create(IafNeuron.withParams(Ie = stim))
  val neuron2 = create(IafNeuron.default)
  val multimeter = create(Multimeter.withRecorders[IafNeuron](
    "Vm" -> (_.Vm)
  ))

  connect(neuron1, neuron2, StaticSynapse.withParams(weight = weight, delay = delay))

  simulate(100 milliseconds)

  val records = data(multimeter)
  val p = Figure().subplot(0)
  val (xs, ys) = records("Vm").unzip
  p += plot(xs, ys, '.')

}
