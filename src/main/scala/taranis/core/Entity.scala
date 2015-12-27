package taranis.core

import akka.actor.{Actor, ActorLogging}
import taranis.core.Network.{AckTick, Calibrate, Tick}
import taranis.core.dynamics.Dynamics

trait Entity extends Actor with ActorLogging with Dynamics {

  override def receive: Receive = {

    case Tick(time) =>
      update(time)
      sender ! AckTick

    case Calibrate(resolution) =>
      calibrate(resolution)

  }

}
