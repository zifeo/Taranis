package taranis

import java.util.logging.LogManager
import javax.swing.SwingUtilities

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import breeze.plot.Plot
import taranis.core.EventsHub.{BindPriors, BindSuccessors}
import taranis.core.Network.{BindEntity, Simulate}
import taranis.core.Records.RecordedData
import taranis.core._
import taranis.core.dynamics.EventDynamics
import taranis.models.devices.Multimeter.{BindRecorder, Metrics}
import taranis.models.synapses.StaticSynapse

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps

package object dsl {

  LogManager.getLogManager.readConfiguration()
  private lazy implicit val system = ActorSystem("Taranis")

  private var identity = 0
  private val network = system.actorOf(Network.props, "network")

  private def spawnE[T <: Entity](elem: Forge[T]): ActorRef = {
    identity += 1
    val className = elem.forgee.getSimpleName
    val ref = system.actorOf(Props(elem.forgee, elem), s"$identity:$className")
    if (classOf[Neuron].isAssignableFrom(elem.forgee))
      network ! BindEntity(ref)
    ref
  }

  private def spawnS[T <: Synapse](elem: Forge[T]): EventDynamics = {
    identity += 1
    val constructor = elem.forgee.getDeclaredConstructors.head
    constructor.setAccessible(true)
    constructor.newInstance(Seq[AnyRef](elem): _*).asInstanceOf[EventDynamics]
  }

  def create[T <: Entity](elem: Forge[T]): ActorRef =
    create(1, elem).head

  def create[T <: Entity](number: Int, elem: Forge[T]): List[ActorRef] = {

    @tailrec
    def rec(current: Int, acc: List[ActorRef]): List[ActorRef] =
      if (current >= number) acc
      else rec(current + 1, spawnE(elem) :: acc)

    rec(0, List.empty)
  }

  def connectD(pre: ActorRef, post: ActorRef): Unit =
    pre ! BindRecorder(post)

  def connect(pre: ActorRef, post: ActorRef): Unit =
    connect(List(pre), List(post), StaticSynapse.default, StaticSynapse.default)

  def connect[T <: Synapse](pre: ActorRef, post: ActorRef, predyn: Forge[T]): Unit =
    connect(List(pre), List(post), predyn, StaticSynapse.default)

  def connect[T <: Synapse](pre: ActorRef, posts: List[ActorRef], predyn: Forge[T]): Unit =
    connect(List(pre), posts, predyn, StaticSynapse.default)

  def connect[T <: Synapse](pres: List[ActorRef], post: ActorRef, predyn: Forge[T]): Unit =
    connect(pres, List(post), predyn, StaticSynapse.default)

  def connect[T <: Synapse](pres: List[ActorRef], posts: List[ActorRef], predyn: Forge[T], postdyn: Forge[T]): Unit = {
    pres.foreach(_ ! BindSuccessors(posts.map(_ -> spawnS(predyn))))
    posts.foreach(_ ! BindPriors(pres.map(_ -> spawnS(postdyn))))
  }

  def simulate(time: Duration, resolution: Duration = Network.defaultResolution): Unit = {
    val termination = Promise[Unit]()
    network ! Simulate(time, resolution, termination)
    Await.result(termination.future, Duration.Inf)
  }

  def data(device: ActorRef): RecordedData = {
    implicit val timeout = Timeout(10 seconds)
    val result = device ? Metrics
    Await.result(result.mapTo[RecordedData], timeout.duration)
  }

  def noGUI(plot: Plot): Unit = {
    val window = SwingUtilities.windowForComponent(plot.panel)
    window.setVisible(true)
    window.dispose()
  }

  def terminate(): Unit =
    Await.result(system.terminate(), Duration.Inf)

}
