package taranis.models.devices

import taranis.core.events.Spike
import taranis.core.{Forge, Neuron, Time}
import taranis.models.devices.Multimeter.Metrics
import taranis.models.devices.SpikeDetector.withParams

import scala.collection.mutable

object SpikeDetector {

  case class withParams() extends Forge[SpikeDetector]

  object default extends withParams

}

final class SpikeDetector(params: withParams) extends Neuron {

  val history = mutable.ListBuffer.empty[(Time, Double)]

  override def receive: Receive = {

    val catchSpike = { case Spike(time, _, _) =>
      history += time -> sender.hashCode()
    }: Receive

    val getMetrics = { case Metrics =>
      sender ! Map("spikes" -> history.toList)
    }: Receive

    catchSpike.orElse(super.receive).orElse(getMetrics)
  }

  override def calibrate(resolution: Time): Unit = super.calibrate(resolution)

  override def update(time: Time): Unit = super.update(time)

}
