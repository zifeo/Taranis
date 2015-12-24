package taranis.benchmark

import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout
import taranis.benchmark.EventBusVsSend.{Incr, Total}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class IncrActor extends Actor {

  var count = 0

  override def receive: Actor.Receive = {
    case Incr =>
      count += 1
    case Total =>
      sender ! count
  }
}

object EventBusVsSend extends App {

  case object Incr
  case object Total

  val threshold = 100
  val actorCount = 100
  implicit val timeout = Timeout(5 seconds)

  val eventBusTime = bench { implicit system =>
    val actors = spawn(Props(classOf[IncrActor]), actorCount)
    actors.foreach(system.eventStream.subscribe(_, Incr.getClass))

    var i = 0
    while (i < threshold) {
      system.eventStream.publish(Incr)
      i += 1
    }

    actors.foreach { actor =>
      val count = actor ? Total
      assert(Await.result(count.mapTo[Int], Duration.Inf) == threshold)
    }
  }

  val sendTime = bench { implicit system =>
    val actors = spawn(Props(classOf[IncrActor]), actorCount)
    actors.foreach(system.eventStream.subscribe(_, Incr.getClass))

    var i = 0
    while (i < threshold) {
      actors.foreach(_ ! Incr)
      i += 1
    }

    actors.foreach { actor =>
      val count = actor ? Total
      assert(Await.result(count.mapTo[Int], Duration.Inf) == threshold)
    }
  }

  println(s"event bus: $eventBusTime")
  println(s"send: $sendTime")

  // event bus: 188.4252598 ms
  // send: 9.8450212 ms

}