package taranis.models

import akka.actor.{ActorRef, Actor, ActorLogging}
import taranis.events.Spike

trait Node extends Actor with ActorLogging {

  def calibrate(resolution: Double): Unit

  def update(origin: Double): Unit

  def handle(e: Spike): Unit

}

object Node {

  final case class Register(ref: ActorRef)

}