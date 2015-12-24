package taranis.benchmark

import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout
import taranis.benchmark.BoxingVsRaw.{Incr, Plus, Total}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class RawActor extends Actor {

  var count = 0

  override def receive: Actor.Receive = {
    case Incr =>
      count += 1
    case Total =>
      sender ! count
  }
}

class BoxingActor extends Actor {

  var count = 0

  override def receive: Actor.Receive = {
    case Plus(i) =>
      count += i
    case Total =>
      sender ! count
  }
}

object BoxingVsRaw extends App {

  case object Incr
  case class Plus(num: Int)
  case object Total

  val threshold = 100
  val actorCount = 100
  implicit val timeout = Timeout(5 seconds)

  val boxingTime = bench { implicit system =>
    val actors = spawn(Props(classOf[BoxingActor]), actorCount)

    var i = 0
    while (i < threshold) {
      val plus = Plus(1)
      actors.foreach(_ ! plus)
      i += 1
    }

    actors.foreach { actor =>
      val count = actor ? Total
      assert(Await.result(count.mapTo[Int], Duration.Inf) == threshold)
    }
  }

  val rawTime = bench { implicit system =>
    val actors = spawn(Props(classOf[RawActor]), actorCount)

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

  println(s"boxing: $boxingTime")
  println(s"raw: $rawTime")

  // boxing: 3977.5629669900004 ms
  // raw: 4360.2027032900005 ms

}