package taranis.benchmarks

import akka.actor.{Actor, Props}
import taranis.benchmarks.StateVsMutable.Incr

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise}

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

  case object Incr

  val threshold = 100000

  val stateTime = bench { implicit system =>
    val termination = Promise[Unit]()
    val state = system.actorOf(Props(classOf[StateIncr], termination, threshold))
    state ! Incr
    Await.result(termination.future, Duration.Inf)
  }

  val mutableTime = bench { implicit system =>
    val termination = Promise[Unit]()
    val mutable = system.actorOf(Props(classOf[MutableIncr], termination, threshold))
    mutable ! Incr
    Await.result(termination.future, Duration.Inf)
  }

  println(s"state: $stateTime")
  println(s"mutable: $mutableTime")

  // state: 22.638966228000047 ms
  // mutable: 19.53756763299998 ms

}