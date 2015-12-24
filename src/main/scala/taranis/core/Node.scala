package taranis.core

import akka.actor.{Actor, ActorLogging, ActorRef}
import taranis.core.events.Spike

trait Node extends Actor with ActorLogging {

  def calibrate(resolution: Double): Unit

  def update(origin: Double): Unit

  def handle(e: Spike): Unit

}

object Node {

  final case class Register(ref: ActorRef)

}