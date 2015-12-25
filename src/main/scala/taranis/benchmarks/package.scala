package taranis

import akka.actor.{ActorRef, ActorSystem, Props}
import org.scalameter._

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration

package object benchmarks {

  val meter = config(
    Key.exec.benchRuns -> 100,
    Key.verbose -> true
  ) withWarmer {
    new Warmer.Default
  } withMeasurer {
    new Measurer.IgnoringGC
  }

  def bench(marks: ActorSystem => Unit): Quantity[Double] = {
    val system = ActorSystem("Taranis-benchmark")
    val measured = meter measure marks(system)
    Await.result(system.terminate(), Duration.Inf)
    measured
  }

  def spawn(props: Props, number: Int)(implicit system: ActorSystem): List[ActorRef] = {

    @tailrec
    def rec(left: Int, acc: List[ActorRef]): List[ActorRef] =
      if (left <= 0) acc
      else rec(left - 1, system.actorOf(props) :: acc)

    rec(number, List.empty)
  }

}