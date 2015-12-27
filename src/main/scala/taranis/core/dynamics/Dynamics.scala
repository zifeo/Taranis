package taranis.core.dynamics

import taranis.core._

trait Dynamics {

  def calibrate(resolution: Time): Unit = ()

  def update(time: Time): Unit = ()

}
