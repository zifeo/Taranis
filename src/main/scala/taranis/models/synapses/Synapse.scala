package taranis.models.synapses

import akka.actor.ActorRef
import taranis.Network.{AckTick, Calibrate, Tick}
import taranis.events.Spike
import taranis.models.Node

abstract class Synapse extends Node {

  import Node._

  private var postSynaptic: ActorRef = _

  override def receive: Receive = {

    case Register(neuron) =>
      postSynaptic = neuron
      //log.debug(s"$self register post-synaptic $neuron")

    case Calibrate(resolution) =>
      calibrate(resolution)

    case Tick(time) =>
      update(time)
      sender ! AckTick

    case spike: Spike =>
      handle(spike)

  }

  def calibrate(resolution: Double): Unit = ()

  def update(origin: Double): Unit = ()

  def handle(e: Spike): Unit = {
    postSynaptic ! bridged(e)
  }

  def bridged(e: Spike): Spike

}
