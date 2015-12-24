package taranis

import java.util.logging.LogManager
import javax.swing.SwingUtilities

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import breeze.plot.Plot
import taranis.Network.{DeviceRequest, Simulate}
import taranis.models.Node.Register
import taranis.models.Parameters
import taranis.models.devices.Multimeter.Records

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps

abstract class Simulation extends App {

  LogManager.getLogManager.readConfiguration()
  implicit val system = ActorSystem("Taranis")

  private var identity = 0
  private val net = system.actorOf(Network.props, "net")

  private def spawn(elem: Parameters): ActorRef = {
    val kind = elem.kind.getSimpleName
    identity += 1
    val ref = system.actorOf(Props(elem.kind, elem), s"$identity:$kind")
    net ! Register(ref)
    ref
  }

  def create(n: Parameters): ActorRef = {
    spawn(n)
  }

  def connect(pre: ActorRef, post: ActorRef, bridge: Parameters): ActorRef = {
    val sRef = spawn(bridge)
    pre ! Register(sRef)
    sRef ! Register(post)
    sRef
  }

  def connect(pre: ActorRef, post: ActorRef): Unit = {
    pre ! Register(post)
  }

  def simulate(time: Duration, resolution: Duration = Network.defaultResolution): Unit = {
    val termination = Promise[Unit]()
    net ! Simulate(termination, time, resolution)
    Await.result(termination.future, Duration.Inf)
  }

  def data(device: ActorRef): Records = {
    val promise = Promise[Records]()
    net ! DeviceRequest(device, promise)
    Await.result(promise.future, Duration.Inf)
  }

  def terminate(): Unit = {
    net ! PoisonPill
    Await.result(system.terminate(), Duration.Inf)
    println("terminate system")
  }

  def cancelGUI(onePlot: Plot): Unit = {
    val window = SwingUtilities.windowForComponent(onePlot.panel)
    window.setVisible(true)
    window.dispose()
  }

}
