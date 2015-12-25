package taranis.core

import akka.actor.ActorRef
import taranis.core.Network.{AckTick, Calibrate, Tick}
import taranis.core.events.Spike

abstract class Synapse extends Node {

  import Node._

  private var successor: ActorRef = _

  override def receive: Receive = {

    case Register(succ) =>
      successor = succ
      log.debug(s"register: $succ")

    case Calibrate(resolution) =>
      calibrate(resolution)

    case Tick(time) =>
      update(time)
      sender ! AckTick

    case spike: Spike =>
      successor ! bridged(spike)

  }

  def calibrate(resolution: Time): Unit = ()

  def update(time: Time): Unit = ()

  def handle(spike: Spike): Unit = ()

  def bridged(spike: Spike): Spike

}
