package taranis.models.neurons

import akka.actor.ActorRef
import taranis.Network.{AckTick, Calibrate, Tick}
import taranis.events.Spike
import taranis.models.Node
import taranis.models.devices.Multimeter

import scala.collection.mutable

abstract class Neuron extends Node {

  import Node._
  import Multimeter._

  private val successors = mutable.ListBuffer.empty[ActorRef]
  protected var spikesValue = 0d
  private val recorders = mutable.Map.empty[(ActorRef, String), Extractor[Neuron]]

  override def receive: Receive = {

    case Register(successor) =>
      successors += successor
      log.debug(s"$self register $successor")

    case Calibrate(resolution) =>
      calibrate(resolution)

    case Tick(time) =>
      update(time)
      spikesValue = 0
      sender ! AckTick

    case spike: Spike =>
      handle(spike)

    case Recorder(label, extractor) =>
      log.debug(s"add recorders $label")
      recorders += (sender, label) -> extractor

  }

  def calibrate(resolution: Double): Unit

  def update(origin: Double): Unit

  def handle(e: Spike): Unit = {
    val weight = e.weight
    log.debug(s"got spike with weight $weight from $sender")
    spikesValue += e.weight * e.multiplicity
  }

  def records(instance: Neuron, origin: Double): Unit = {
    recorders.foreach {
      case ((sender, label), extractor) =>
        sender ! Data(origin, label, extractor(instance))

    }
  }

  protected def send(spike: Spike): Unit = {
    successors.foreach(_ ! spike)
  }

}
