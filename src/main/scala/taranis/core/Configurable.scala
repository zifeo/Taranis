package taranis.core

import akka.event.LoggingAdapter

trait Configurable {

  val log: LoggingAdapter



  def receive: PartialFunction[Any, Unit]

}
