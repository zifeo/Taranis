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
import taranis.models.synapses.IdentityDynamics

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps
import scala.util.Random

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

  private def spawnS[T <: EventDynamics](elem: Forge[T]): EventDynamics = {
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

  def connectDevice(pre: ActorRef, post: ActorRef): Unit =
    pre ! BindRecorder(post)

  def connect(pre: ActorRef, post: ActorRef): Unit =
    connect(List(pre), List(post), IdentityDynamics.default, IdentityDynamics.default, 0)

  def connect[T <: EventDynamics](pre: ActorRef, post: ActorRef, predyn: Forge[T]): Unit =
    connect(List(pre), List(post), predyn, IdentityDynamics.default, 0)

  def connect[T <: EventDynamics](pre: ActorRef, posts: List[ActorRef], predyn: Forge[T]): Unit =
    connect(List(pre), posts, predyn, IdentityDynamics.default, 0)

  def connect[T <: EventDynamics](pres: List[ActorRef], post: ActorRef, predyn: Forge[T]): Unit =
    connect(pres, List(post), predyn, IdentityDynamics.default, 0)

  def connect[T <: EventDynamics](pres: List[ActorRef], posts: List[ActorRef], predyn: Forge[T], mapping: Int): Unit =
    connect(pres, posts, predyn, IdentityDynamics.default, mapping)

  def connect[T <: EventDynamics](pres: List[ActorRef], posts: List[ActorRef], predyn: Forge[T], postdyn: Forge[T], mapping: Int): Unit =
    mapping match {
      case 0 => // all to all
        pres.foreach(_ ! BindSuccessors(posts.map(_ -> spawnS(predyn))))
        posts.foreach(_ ! BindPriors(pres.map(_ -> spawnS(postdyn))))
      case 1 => // one to one
        require(pres.size == posts.size, "pres and posts list must have the same size with one to one")
        pres.zip(posts).foreach { case (pre, post) =>
          pre ! BindSuccessors(List(post -> spawnS(predyn)))
          post ! BindPriors(List(pre -> spawnS(postdyn)))
        }
      case n =>
        posts.foreach { post =>
          val selected = Random.shuffle(pres).take(n)
          selected.foreach { pre =>
            pre ! BindSuccessors(List(post -> spawnS(predyn)))
          }
          post ! BindPriors(selected.map(_ -> spawnS(postdyn)))
        }
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
