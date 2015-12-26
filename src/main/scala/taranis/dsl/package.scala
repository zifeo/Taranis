package taranis

import java.util.logging.LogManager
import javax.swing.SwingUtilities

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import breeze.plot.Plot
import taranis.core.Entity.Register
import taranis.core.Network.Simulate
import taranis.core.Recordable.Records
import taranis.core.{Entity, Forge, Network}
import taranis.models.devices.Multimeter.Request

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps

package object dsl {

  LogManager.getLogManager.readConfiguration()
  private lazy implicit val system = ActorSystem("Taranis")

  private var identity = 0
  private val network = system.actorOf(Network.props, "network")

  private def spawn[T](elem: Forge[T]): ActorRef = {
    identity += 1
    val className = elem.forgee.getSimpleName
    val ref = system.actorOf(Props(elem.forgee, elem), s"$identity:$className")

    if (classOf[Entity].isAssignableFrom(elem.forgee))
      network ! Register(ref)

    ref
  }

  def create[T](number: Int)(n: Forge[T]): List[ActorRef] = {
    ???
  }

  def create[T](n: Forge[T]): ActorRef = {
    spawn(n)
  }

  def connect(pre: ActorRef, post: ActorRef): Unit = {
    pre ! Register(post)
  }

  def connect[T](pre: ActorRef, post: ActorRef, bridge: Forge[T]): ActorRef = {
    val bridger = spawn(bridge)
    connect(pre, bridger)
    connect(bridger, post)
    bridger
  }

  def connect[T](pre: ActorRef, posts: List[ActorRef], bridge: Forge[T]): List[ActorRef] = {
    ???
  }

  def connect[T](pres: List[ActorRef], post: ActorRef, bridge: Forge[T]): List[ActorRef] = {
    ???
  }

  def connect[T](pres: List[ActorRef], posts: List[ActorRef], bridge: Forge[T]): List[ActorRef] = {
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
