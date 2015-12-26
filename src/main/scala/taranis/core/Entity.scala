package taranis.core

import akka.actor.{Actor, ActorLogging, ActorRef}
import taranis.core.dynamics.Dynamics
import taranis.core.events.Spike

import scala.collection.mutable

trait Entity extends Actor with ActorLogging with Dynamics {

  //protected val priors = mutable.AnyRefMap.empty[ActorRef, Dynamics]
  //protected val successors = mutable.AnyRefMap.empty[ActorRef, Dynamics]

  def calibrate(resolution: Time): Unit

  def update(time: Time): Unit

  def handle(spike: Spike): Unit

}

object Entity {

  final case class Register(node: ActorRef)

}