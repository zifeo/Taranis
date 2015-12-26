package taranis.core

import akka.actor.ActorRef
import taranis.core.Network.{AckTick, Calibrate, Tick}
import taranis.core.events.Spike
import taranis.models.devices.Multimeter

import scala.collection.mutable

abstract class Neuron extends Entity {

  import Multimeter._
  import Entity._

  //private val priors = mutable.AnyRefMap.empty[ActorRef, Node]
  //private val successors = mutable.AnyRefMap.empty[ActorRef, Node]

  private val successors = mutable.ListBuffer.empty[ActorRef]

  private val recorders = mutable.Map.empty[ActorRef, List[Extractor[Neuron]]]
  protected var bufferedSpike = 0d

  override def receive: Receive = {

    case Register(successor) =>
      successors += successor
      log.debug(s"register: $successor")

    case Record(extractor) =>
      recorders += sender -> (extractor :: recorders.getOrElse(sender, List.empty))
      val label = extractor._1
      log.debug(s"record: $label")

    case Calibrate(resolution) =>
      calibrate(resolution)

    case Tick(time) =>
      update(time)
      bufferedSpike = 0
      records(time)
      sender ! AckTick

    case spike: Spike =>
      handle(spike)

  }

  def handle(spike: Spike): Unit =
    bufferedSpike += spike.weight

  def records(time: Time): Unit =
    for {
      (recorder, extractors) <- recorders
      (label, extractor) <- extractors
    } {
      recorder ! Data(time, label, extractor(this))
    }

  protected def send(spike: Spike): Unit =
    successors.foreach(_ ! spike)


}
