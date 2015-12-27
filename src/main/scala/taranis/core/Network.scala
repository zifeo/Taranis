package taranis.core

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.language.postfixOps

class Network extends Actor with ActorLogging {

  import Network._

  private val nodes = mutable.Set.empty[ActorRef]

  private var tickAcks = 0
  private var nextTick = 0: Time

  def receive: Receive = setup

  def setup: Receive = {

    case BindEntity(entity) =>
      nodes += entity
      log.debug(s"register: $entity")

    case Simulate(duration, resolution, termination) =>
      tickAcks = 0
      nextTick = 0
      context.become(simulating(
        nodes.size,
        duration.toUnit(MILLISECONDS),
        resolution.toUnit(MILLISECONDS),
        termination
      ))
      log.debug(s"start simulation: $duration at $resolution")

  }

  def simulating(networkSize: Int, duration: Time, resolution: Time, termination: Promise[Unit]): Receive = {

    val calibration = Calibrate(resolution)
    nodes.foreach(_ ! calibration)
    tickNodes()

    def tickNodes(): Unit = {
      val tick = Tick(nextTick)
      nodes.foreach(_ ! tick)
      nextTick += resolution
      tickAcks = 0
    }

    { case AckTick =>
      tickAcks += 1

      if (tickAcks == networkSize) {
        if (nextTick < duration)
          tickNodes()
        else {
          termination.success(())
          context.become(setup)
          log.debug(s"terminate simulation")
        }
      }
    }
  }

}

object Network {

  val defaultResolution = 1 millisecond

  def props: Props =
    Props(new Network)

  final case class BindEntity(entity: ActorRef)

  final case class Simulate(duration: Duration, resolution: Duration = defaultResolution, termination: Promise[Unit])

  final case class Calibrate(resolution: Time)

  final case class Tick(time: Time)

  object AckTick

}