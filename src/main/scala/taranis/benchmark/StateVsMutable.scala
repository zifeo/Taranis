package taranis.benchmark

import akka.actor.{Actor, ActorSystem, Props}
import org.scalameter._
import taranis.benchmark.StateVsMutable.Incr

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration.Duration

class StateIncr(termination: Promise[Unit], threshold: Int) extends Actor {

  override def receive: Receive = state(0)

  def state(count: Int): Receive = {
    case Incr if count > threshold =>
      termination.success(())
    case Incr =>
      context.become(state(count + 1))
      self ! Incr
  }
}

class MutableIncr(termination: Promise[Unit], threshold: Int) extends Actor {

  var count = 0

  override def receive: Actor.Receive = {
    case Incr if count > threshold =>
      termination.success(())
    case Incr =>
      count += 1
      self ! Incr
  }
}

object StateVsMutable extends App {

  implicit val system = ActorSystem("Taranis-benchmark")
  case object Incr

  val threshold = 100000
  val bench = config(
    Key.exec.benchRuns -> 1000,
    Key.verbose -> true
  ) withWarmer {
    new Warmer.Default
  } withMeasurer {
    new Measurer.IgnoringGC
  }

  val stateTime = bench measure {
    val termination = Promise[Unit]()
    val state = system.actorOf(Props(classOf[StateIncr], termination, threshold))
    state ! Incr
    Await.result(termination.future, Duration.Inf)
  }

  val mutableTime = bench measure {
    val termination = Promise[Unit]()
    val mutable = system.actorOf(Props(classOf[MutableIncr], termination, threshold))
    mutable ! Incr
    Await.result(termination.future, Duration.Inf)
  }

  println(s"state: $stateTime")
  println(s"mutable: $mutableTime")

  // state: 22.638966228000047 ms
  // mutable: 19.53756763299998 ms

  Await.result(system.terminate(), Duration.Inf)

}