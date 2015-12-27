package taranis.core

import scala.reflect.ClassTag

/** Parameters container for dynamics. */
abstract class Forge[+T: ClassTag] {

  val forgee = implicitly[ClassTag[T]].runtimeClass

}
