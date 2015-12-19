package taranis

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import taranis.models.Node.Register
import taranis.models.devices.Multimeter
import taranis.models.devices.Multimeter.Records

import scala.collection.mutable
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.language.postfixOps

class Network extends Actor with ActorLogging {

  import Multimeter._
  import Network._
  import context._

  private val nodes = mutable.Set.empty[ActorRef]

  def receive: Receive = setup

  def setup: Receive = {

    case Register(node) =>
      nodes += node

    case Simulate(time, resolution) =>
      log.debug(s"starting simulation with resolution $resolution and time $time")

      nodes.foreach(_ ! Calibrate(resolution.toMillis))
      remainingSimulationTime = time.toMillis
      tick(resolution.toMillis)

      become(simulating(time.toMillis, resolution.toMillis))

    case DeviceRequest(device, promise) =>
      device ! Request(self)
      become(requesting(promise))

  }

  def requesting(promise: Promise[Records]): Receive = {

    case Results(data) =>
      promise.success(data)
      become(setup)

  }

  private var remainingSimulationTime = 0l
  private val ackTicks = mutable.Set.empty[ActorRef]

  def simulating(time: Long, resolution: Long): Receive = {

    case AckTick =>
      ackTicks += sender()

      if (ackTicks.size == nodes.size) {
        if (remainingSimulationTime > 0) {
          log.debug(s"advance simulation $remainingSimulationTime")
          tick(time - remainingSimulationTime)
        } else {
          log.debug(s"terminate simulation")
          become(setup)
        }
      }

  }

  def tick(step: Long): Unit = {
    nodes.foreach(_ ! Tick(step))
    remainingSimulationTime -= step
  }

}

object Network {

  val defaultResolution = 1 millisecond

  def props: Props =
    Props(new Network)

  final case class Simulate(time: Duration, resolution: Duration = defaultResolution)

  final case class Tick(time: Long)

  case object AckTick

  final case class Calibrate(resolution: Long)

  final case class DeviceRequest(device: ActorRef, promise: Promise[Records])

}