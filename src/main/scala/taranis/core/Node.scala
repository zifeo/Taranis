package taranis.core

import akka.actor.{Actor, ActorLogging, ActorRef}
import taranis.core.events.Spike

trait Node extends Actor with ActorLogging {

  def calibrate(resolution: Time): Unit

  def update(time: Time): Unit

  def handle(spike: Spike): Unit

}

object Node {

  final case class Register(node: ActorRef)

}