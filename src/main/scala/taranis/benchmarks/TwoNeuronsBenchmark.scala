package taranis.benchmarks

import kamon.Kamon
import taranis.dsl._
import taranis.models.devices._
import taranis.models.neurons._
import taranis.models.synapses._

import scala.concurrent.duration._
import scala.language.postfixOps

object TwoNeuronsBenchmark extends App {

  Kamon.start()
  val run = bench { _ =>

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
  }

  Thread.sleep(5000)
  Kamon.shutdown()

  println(s"run: $run")
  terminate()

}
