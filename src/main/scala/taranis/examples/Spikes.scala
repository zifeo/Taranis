package taranis.examples

import java.util.logging.LogManager

import akka.actor.ActorSystem
import taranis.Network.Simulate
import taranis.{Network, Neuron}
import taranis.Neuron.Register
import taranis.events.Spike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object Spikes extends App {

  LogManager.getLogManager.readConfiguration()
  implicit val system = ActorSystem("Taranis")

  val n1 = system.actorOf(Neuron.props, "n1")
  val n2 = system.actorOf(Neuron.props, "n2")

  n1 ! Register(n2)

  n1 ! Spike(10)

  val net = system.actorOf(Network.props(Set(n1, n2)), "network")

  net ! Simulate(1 second, 0.2 second)

  Await.result(system.whenTerminated, Duration.Inf)
  system.terminate()

}
