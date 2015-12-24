package taranis

import akka.actor.ActorSystem
import org.scalameter._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

package object benchmark {

  val meter = config(
    Key.exec.benchRuns -> 1000,
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

}