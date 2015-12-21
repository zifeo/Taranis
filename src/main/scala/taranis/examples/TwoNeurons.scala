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
  connect(multimeter, neuron2)

  simulate(100 milliseconds, 1 millisecond)

  val records = data(multimeter)
  println(records)
  val (xs, ys) = records("Vm").unzip

  val f = Figure()
  val p = f.subplot(0)
  p.xlabel = "Time (ms)"
  p.ylabel = "Membrane potential (mV)"
  p.legend = true
  p.title = "Membrane potential"
  //p.xlim = (0, 100)
  //p.ylim = (-70, -69.3)
  p.setYAxisDecimalTickUnits()
  p += plot(xs, ys, name = "Neuron 2")

  terminate()
  Thread.sleep(5000)
  //cancelGUI(p)

}