package taranis.core

import akka.actor.ActorRef
import taranis.core.EventsHub.{BindPriors, BindSuccessors}
import taranis.core.dynamics.{Dynamics, EventDynamics}
import taranis.core.events.Event

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/** Mix-in for recording data out of [[Dynamics]]. */
trait EventsHub extends Entity {

  private val priors = mutable.AnyRefMap.empty[ActorRef, EventDynamics]
  protected val successors = mutable.AnyRefMap.empty[ActorRef, EventDynamics]
  private var eventsBuffer = mutable.ArrayBuffer.empty[Event]

  abstract override def receive: Receive = {

    val receiveEvent = { case event: Event =>
      eventsBuffer += event
      if (priors.contains(sender))
        eventsBuffer += priors(sender).handle(event)
    }: Receive

    val manageDynamics = {
      case BindPriors(prios) =>
        priors ++= prios

      case BindSuccessors(succs) =>
        successors ++= succs
    }: Receive

    receiveEvent.orElse(super.receive).orElse(manageDynamics)
  }

  abstract override def calibrate(resolution: Time): Unit = {
    priors.foreachValue(_.calibrate(resolution))
    successors.foreachValue(_.calibrate(resolution))
    super.calibrate(resolution)
  }

  abstract override def update(time: Time): Unit = {
    priors.foreachValue(_.update(time))
    successors.foreachValue(_.update(time))
    super.update(time)
  }

  protected def send(event: Event): Unit = {
    successors.foreach { case (successor, dynamic) =>
      successor ! dynamic.handle(event)
    }
    val info = event.informative
    priors.foreach { case (prior, dynamic) =>
      prior ! info
    }
  }

  protected def events(until: Time): ArrayBuffer[Event] = {
    val (past, future) = eventsBuffer.partition(_.delivery <= until)
    eventsBuffer = future
    past.result()
  }

}

/** [[EventsHub]] companion. */
object EventsHub {

  final case class BindPriors(successors: List[(ActorRef, EventDynamics)])

  final case class BindSuccessors(successors: List[(ActorRef, EventDynamics)])

}
