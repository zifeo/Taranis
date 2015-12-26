package taranis.core.dynamics

import taranis.core._
import taranis.core.events.Event

trait Dynamics {

  def calibrate(resolution: Time): Unit

  def update(time: Time): Unit

  //def handle: PartialFunction[Event, Unit]

}