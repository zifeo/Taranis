package taranis

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import taranis.Network.{AckTick, Tick}
import taranis.Neuron.Register
import taranis.events.Spike

import scala.collection.mutable

class Neuron extends Actor with ActorLogging {

  private var level = 0.0
  private val successors = mutable.ListBuffer.empty[ActorRef]

  private val receivedSpikes = mutable.ListBuffer.empty[Double]

  override def receive: Receive = {

    case Register(successor) =>
      log.debug(s"$self register $successor")
      successors += successor

    case Spike(weight) =>
      log.debug(s"got spike with weight $weight from $sender")
      receivedSpikes += weight

    case Tick(elapsedTime) =>
      val decay: Double = math.exp(- 200 / 100)
      log.debug(s"$sender decay: $decay")
      level -= decay
      level += receivedSpikes.sum
      receivedSpikes.clear()
      log.debug(s"$sender level: $level")

      if (level > 15) {
        successors.foreach(_ ! Spike(10.0))
      }

      sender ! AckTick(Set.empty)

  }

  def spike(weight: Double): Unit =
    successors.foreach(_ ! Spike(weight))



}

object Neuron {

  def props: Props =
    Props(new Neuron)

  final case class Register(successor: ActorRef)

}