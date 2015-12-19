package taranis

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

class Network(nodes: Set[ActorRef]) extends Actor with ActorLogging {

  import Network._
  import context._

  override def receive: Receive = setup

  def setup: Receive = {

    case Simulate(time, resolution) =>
      log.debug(s"starting simulation with resolution $resolution and time $time")
      become(simulating(time.toNanos, resolution.toNanos))
      remainingSimulationTime = time.toNanos
      advanceTick(resolution.toNanos)

  }

  // ns
  private var remainingSimulationTime = 0l
  private val ackTicks = mutable.Set.empty[ActorRef]

  def simulating(time: Long, resolution: Long): Receive = {

    case AckTick(_) =>
      ackTicks += sender()

      if (ackTicks.size == nodes.size) {

        if (remainingSimulationTime > 0) {
          advanceTick(resolution)
          log.debug(s"advance simulation $remainingSimulationTime")
        } else {
          log.debug(s"terminate simulation")
          become(setup)
        }

      }

  }

  def advanceTick(step: Long): Unit = {
    nodes.foreach(_ ! Tick(step))
    remainingSimulationTime -= step
  }

}

object Network {

  val defaultResolution = 1 millisecond

  def props(nodes: Set[ActorRef]): Props =
    Props(new Network(nodes))

  final case class Simulate(time: Duration, resolution: Duration = defaultResolution)

  // ns
  final case class Tick(elapsedTime: Long)

  final case class AckTick(spikesSuccessors: Set[ActorRef])

}