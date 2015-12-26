package taranis.benchmarks

import scala.collection.mutable

object ListVsArray extends App {

  val elements = 1000000

  val list = bench { _ =>
    val range = List.range(0, elements)
    range.foreach(_ + 1)
  }

  val array = bench { _ =>
    val range = mutable.ArrayBuffer.range(0, elements)
    range.foreach(_ + 1)
  }

  println(s"list: $list")
  println(s"array: $array")

}
