package taranis.core

import akka.actor.ActorRef
import taranis.core.Network.{AckTick, Calibrate, Tick}
import taranis.core.events.Spike

abstract class Synapse extends Entity {

  import Entity._

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
      handle(spike)

  }

  def send(spike: Spike): Unit =
    successor ! spike

}
