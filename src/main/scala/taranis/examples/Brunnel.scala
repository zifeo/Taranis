package taranis.examples

import breeze.plot._
import taranis.dsl._
import taranis.models.devices._
import taranis.models.neurons._
import taranis.models.synapses._

import scala.concurrent.duration._
import scala.language.postfixOps

object Brunnel extends App {

  val dt      = 0.1 millisecond   // the resolution in ms
  val simtime = 1000 milliseconds // Simulation time in ms
  val delay   = 1.5    // synaptic delay in ms

  val g       = 5.0  // ratio inhibitory weight/excitatory weight
  val eta     = 2.0  // external rate relative to threshold rate
  val epsilon = 0.1  // connection probability

  val order     = 2500
  val NE        = 4*order // number of excitatory neurons
  val NI        = 1*order // number of inhibitory neurons
  val N_neurons = NE+NI   // number of neurons in total
  val N_rec     = 50      // record from 50 neurons

  val CE    = (epsilon*NE).toInt // number of excitatory synapses per neuron
  val CI    = (epsilon*NI).toInt // number of inhibitory synapses per neuron
  val C_tot = CI+CE              // total number of synapses per neuron

  val tauMem = 20.0   // time constant of membrane potential in ms
  val theta  = 20.0   // membrane threshold potential in mV
  val J     = 0.1     // postsynaptic amplitude in mV
  val J_ex  = J       // amplitude of excitatory postsynaptic potential
  val J_in  = -g*J_ex // amplitude of inhibitory postsynaptic potential

  val nu_th  = theta/(J*CE*tauMem)
  val nu_ex  = eta*nu_th
  val p_rate = 1000.0*nu_ex*CE

  val neuron = IafPscDelta.withParams(
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

  val nodes_ex = create(NE)(neuron)
  val nodes_in = create(NI)(neuron)
  val noise    = create(PoissonGenerator.withParams(
    rate = p_rate
  ))
  val espikes  = create(SpikeDetector.default)
  val ispikes  = create(SpikeDetector.default)

  connect(noise, nodes_ex, excitatory)
  connect(noise, nodes_in, excitatory)

  connect(nodes_ex.dropRight(N_rec), espikes, excitatory)
  connect(nodes_in.dropRight(N_rec), ispikes, excitatory)

  //conn_params_ex = {'rule': 'fixed_indegree', 'indegree': CE}
  connect(nodes_ex, nodes_ex ++ nodes_in, conn_params_ex, excitatory)

  //conn_params_in = {'rule': 'fixed_indegree', 'indegree': CI}
  connect(nodes_in, nodes_ex ++ nodes_in, conn_params_in, inhibitory)

  simulate(simtime, dt)

  val events_ex = data(espikes)("n_events")

  nest.raster_plot.from_device(espikes, hist=True)

}
