package taranis

import java.util.logging.LogManager
import javax.swing.SwingUtilities

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import breeze.plot.Plot
import taranis.core.Network.Simulate
import taranis.core.Node.Register
import taranis.core.{Network, Node, Parameters}
import taranis.models.devices.Multimeter.{Records, Request}

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps

package object dsl {

  LogManager.getLogManager.readConfiguration()
  private lazy implicit val system = ActorSystem("Taranis")

  private var identity = 0
  private val network = system.actorOf(Network.props, "network")

  private def spawn(elem: Parameters): ActorRef = {
    identity += 1
    val className = elem.kind.getSimpleName
    val ref = system.actorOf(Props(elem.kind, elem), s"$identity:$className")

    if (classOf[Node].isAssignableFrom(elem.kind))
      network ! Register(ref)

    ref
  }

  def create(number: Int)(n: Parameters): List[ActorRef] = {
    ???
  }

  def create(n: Parameters): ActorRef = {
    spawn(n)
  }

  def connect(pre: ActorRef, post: ActorRef): Unit = {
    pre ! Register(post)
  }

  def connect(pre: ActorRef, post: ActorRef, bridge: Parameters): ActorRef = {
    val bridger = spawn(bridge)
    connect(pre, bridger)
    connect(bridger, post)
    bridger
  }

  def connect(pre: ActorRef, posts: List[ActorRef], bridge: Parameters): List[ActorRef] = {
    ???
  }

  def connect(pres: List[ActorRef], post: ActorRef, bridge: Parameters): List[ActorRef] = {
    ???
  }

  def connect(pres: List[ActorRef], posts: List[ActorRef], bridge: Parameters): List[ActorRef] = {
    ???
  }


  def simulate(time: Duration, resolution: Duration = Network.defaultResolution): Unit = {
    val termination = Promise[Unit]()
    network ! Simulate(time, resolution, termination)
    Await.result(termination.future, Duration.Inf)
  }

  def data(device: ActorRef): Records = {
    implicit val timeout = Timeout(10 seconds)
    val result = device ? Request
    Await.result(result.mapTo[Records], timeout.duration)
  }

  def noGUI(plot: Plot): Unit = {
    val window = SwingUtilities.windowForComponent(plot.panel)
    window.setVisible(true)
    window.dispose()
  }

  def terminate(): Unit =
    Await.result(system.terminate(), Duration.Inf)

}
