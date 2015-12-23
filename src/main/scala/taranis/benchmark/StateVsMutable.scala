package taranis.benchmark

import akka.actor.{Actor, ActorSystem, Props}
import org.scalameter._
import taranis.benchmark.StateVsMutable.Incr

import scala.concurrent.Promise

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

  implicit val system = ActorSystem("Taranis")
  case object Incr

  val threshold = 100000000
  val bench = config(
    Key.exec.benchRuns -> 10000,
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
  }

  val mutableTime = bench measure {
    val termination = Promise[Unit]()
    val mutable = system.actorOf(Props(classOf[MutableIncr], termination, threshold))
    mutable ! Incr
  }

  println(s"state: $stateTime")
  println(s"mutable: $mutableTime")

}