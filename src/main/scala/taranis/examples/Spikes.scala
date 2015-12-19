package taranis.examples

import akka.actor.ActorSystem
import taranis.Neuron
import taranis.events.Spike

object Spikes extends App {

  implicit val system = ActorSystem("Taranis")

  val n1 = system.actorOf(Neuron.props)
  val n2 = system.actorOf(Neuron.props)

  n1 ! Spike(10)

}
