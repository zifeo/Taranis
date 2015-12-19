package taranis

import akka.actor.{Props, ActorLogging, Actor}
import taranis.events.Spike

class Neuron extends Actor with ActorLogging {

  override def receive: Receive = {
    case Spike(weight) =>

  }

}

object Neuron {

  def props: Props =
    Props(classOf[Neuron])

}