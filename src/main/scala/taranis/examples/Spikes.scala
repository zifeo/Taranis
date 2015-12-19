package taranis.examples

import akka.actor.ActorSystem
import taranis.Neuron
import taranis.Neuron.Register
import taranis.events.Spike

import scala.io.StdIn

object Spikes extends App {

  implicit val system = ActorSystem("Taranis")

  val n1 = system.actorOf(Neuron.props, "n1")
  val n2 = system.actorOf(Neuron.props, "n2")

  n1 ! Register(n2)

  n1 ! Spike(10)
  n1 ! Spike(10)

  system.terminate()

}
