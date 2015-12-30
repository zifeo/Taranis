package taranis.examples

import java.awt.Color

import breeze.plot._
import taranis.dsl._
import taranis.models.devices._
import taranis.models.neurons._
import taranis.models.synapses._

import scala.concurrent.duration._
import scala.language.postfixOps

object Brunnel extends App {

  val startBuild = System.nanoTime()

  val dt      = 1 millisecond // the resolution in ms
  val simtime = 1000 milliseconds // Simulation time in ms
  val delay   = 1.5 // synaptic delay in ms

  val g       = 5.0 // ratio inhibitory weight/excitatory weight
  val eta     = 2.0 // external rate relative to threshold rate
  val epsilon = 0.1 // connection probability

  val order     = 250
  val NE        = 4 * order // number of excitatory neurons
  val NI        = 1 * order // number of inhibitory neurons
  val N_neurons = NE + NI // number of neurons in total
  val N_rec     = 50 // record from 50 neurons

  val CE    = (epsilon * NE).toInt // number of excitatory synapses per neuron
  val CI    = (epsilon * NI).toInt // number of inhibitory synapses per neuron
  val C_tot = CI + CE // total number of synapses per neuron

  val tauMem = 20.0 // time constant of membrane potential in ms
  val theta  = 20.0 // membrane threshold potential in mV
  val J     = 0.1 // postsynaptic amplitude in mV
  val J_ex  = J // amplitude of excitatory postsynaptic potential
  val J_in  = -g * J_ex // amplitude of inhibitory postsynaptic potential

  val nu_th  = theta / (J * CE * tauMem)
  val nu_ex  = eta * nu_th
  val p_rate = 1000.0 * nu_ex * CE

  val neuron = IafPscDeltaNeuron.withParams(
    Cm =        1.0,
    tauM =      tauMem,
    tRef =      2.0,
    EL =        0.0,
    Vreset =    0.0,
    Vth =       theta
  )
  val excitatory = StaticSynapse.withParams(
    weight = J_ex,
    delay = delay
  )
  val inhibitory = StaticSynapse.withParams(
    weight = J_in,
    delay = delay
  )

  val nodes_ex = create(NE, neuron)
  val nodes_in = create(NI, neuron)
  val noise    = create(PoissonGenerator.withParams(
    rate = p_rate
  ))
  val espikes  = create(SpikeDetector.default)
  val ispikes  = create(SpikeDetector.default)

  connect(noise, nodes_ex, excitatory)
  connect(noise, nodes_in, excitatory)

  connect(nodes_ex.dropRight(N_rec), espikes, excitatory)
  connect(nodes_in.dropRight(N_rec), ispikes, excitatory)

  connect(nodes_ex, nodes_ex ++ nodes_in, excitatory, mapping = CE)
  connect(nodes_in, nodes_ex ++ nodes_in, inhibitory, mapping = CI)

  val endBuild = System.nanoTime()

  simulate(simtime, dt)

  val endSimulate = System.nanoTime()

  val measureBuildTime = (endBuild - startBuild) / 1e9
  val measureSimtime = (endSimulate - endBuild) / 1e9

  println(s"Building time: $measureBuildTime")
  println(s"Simulation time: $measureSimtime")

  val events_ex = data(espikes)("spikes")
  val selected = events_ex.map(_._2).distinct.take(50).zipWithIndex.toMap
  val selection = events_ex.flatMap { case (spikeTime, nid) =>
    selected.get(nid).map(spikeTime -> _.toDouble)
  }
  val (xs, ys) = selection.unzip
  val f = Figure()

  val p1 = f.subplot(2, 1, 0)
  p1.ylabel = "Neuron ID"
  p1 += scatter(xs, ys, _ => 2, _ => Color.BLUE)
  p1.xaxis.setVisible(false)

  val p2 = f.subplot(2, 1, 1)
  p2 += hist(xs, bins = simtime.toMillis.toInt / 5)
  p2.xlabel = "Time (ms)"
  p2.ylabel = "Rate (hz)"

}
