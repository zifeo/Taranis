package taranis.core.events

final case class EventInfo(event: Event) extends Event {

  val time = event.time

  val delay = event.delay

}
