package taranis.benchmarks

object SingleVsOrElse extends App {

  val range = List.range(0, 1000000)

  val allInOne: PartialFunction[Int, Unit] = {
    case x: Int =>
    //case x: Int if x % 2 == 0 =>
    //case x: Int if x % 3 == 0 =>
  }

  val first: PartialFunction[Int, Unit] = {
    case x: Int =>
  }

  val second: PartialFunction[Int, Unit] = {
    case x: Int if x % 2 == 0 =>
  }

  val third: PartialFunction[Int, Unit] = {
    case x: Int if x % 3 == 0 =>
  }

  val composed = first.orElse(second).orElse(third)

  val single = bench { _ =>
    range.foreach(allInOne)
  }

  val orElse = bench { _ =>
    range.foreach(composed)
  }

  println(s"single: $single")
  println(s"orElse: $orElse")

}
