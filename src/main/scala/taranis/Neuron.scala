package taranis

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import taranis.Neuron.Register
import taranis.events.Spike

import scala.collection.mutable

class Neuron extends Actor with ActorLogging {

  private var potential = 0
  private val successors = mutable.ListBuffer.empty[ActorRef]

  override def receive: Receive = {

    case Register(successor) =>
      log.debug(s"$self register $successor")
      successors += successor

    case Spike(weight) =>
      log.debug(s"got spike with weight $weight from $sender")
      potential += 10
      if (potential > 15) spike(10)

  }

  def spike(weight: Int): Unit =
    successors.foreach(_ ! Spike(weight))

}

object Neuron {

  def props: Props =
    Props(new Neuron)

  final case class Register(successor: ActorRef)

}