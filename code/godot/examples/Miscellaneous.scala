import godot.imperative.Flow._
import scala.concurrent.ExecutionContext.Implicits.global

object Misc {
  def fromFlowToString(): Unit = {
    println("Asynchronously building a String from a Flow[Int] using for-comprehension:\n\n")
    println("```\nval flow = for {\n      flowInt <- asyncS { 42 }\n      flowString <- flowInt.map(_.toString())\n} " +
      "yield flowString\n```")


    val flow = for {
      flowInt <- asyncS { 42 }
      flowString <- flowInt.map(_.toString())
    } yield flowString
    println(s"\n\nResult from 42 to String is: '${flow.getS}'")
  }
}