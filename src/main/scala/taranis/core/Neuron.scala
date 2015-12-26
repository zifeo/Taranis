package taranis.core

import akka.actor.ActorRef
import taranis.core.Network.{AckTick, Calibrate, Tick}
import taranis.core.events.Spike

import scala.collection.mutable

abstract class Neuron extends Entity {

  import Entity._

  //private val priors = mutable.AnyRefMap.empty[ActorRef, Node]
  //private val successors = mutable.AnyRefMap.empty[ActorRef, Node]

  private val successors = mutable.ListBuffer.empty[ActorRef]

  protected var bufferedSpike = 0d

  override def receive: Receive = {

    case Register(successor) =>
      successors += successor
      log.debug(s"register: $successor")

    case Calibrate(resolution) =>
      calibrate(resolution)

    case Tick(time) =>
      update(time)
      bufferedSpike = 0
      sender ! AckTick

    case spike: Spike =>
      handle(spike)

  }

  def handle(spike: Spike): Unit =
    bufferedSpike += spike.weight

  protected def send(spike: Spike): Unit =
    successors.foreach(_ ! spike)


}
