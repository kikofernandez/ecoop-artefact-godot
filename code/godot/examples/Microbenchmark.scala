import godot.imperative._
import godot.imperative.Flow._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Microbenchmark {
  def factorial(n: Int, accumulator: Int): Flow[Int] =
    if (n == 1) accumulator
    else asyncS(factorial(n - 1, n * accumulator))

  // Alternative 1
  def factorialFuture(n: Int, accumulator: Int): Future[Int] = {
    if (n == 1) Future.successful(accumulator)
    else {
      val result = factorialFuture(n - 1, n * accumulator)
      val finalResult = Await.result(result, 1000.millis)
      Future.successful(finalResult)
    }
  }

  // Alternative 2
  def factorialFutureAlt(n: Int, accumulator: Int): Future[Int] = {
    if (n == 1) Future.successful(accumulator)
    else Future(factorialFutureAlt(n - 1, n * accumulator)).flatMap(identity _)
  }

  def fibonacci(n: Int, a: Int, b: Int): Flow[Int] = {
    if (n == 0) return a
    if (n == 1) return b
    asyncS(fibonacci(n - 1, b, a + b))
  }

}


object Main extends App {
  import Microbenchmark._
  println("This program runs the factorial microbenchmark using data-flow futures and control-flow futures.\n")
  println("An asynchronous tail-recursive function for calculating the factorial of a\nnumber cannot be typed unless" +
    " the developer introduces blocking (`get`) or\nawaiting (`Await.ready`) operations. For example, one can type " +
    "the factorial\nfunction using `Futures` as follows.\n\n")
  println("```\nimport scala.concurrent.ExecutionContext.Implicits.global\nimport scala.concurrent.duration" +
    "._\nimport scala.concurrent.{Await, Future}\n\n// Alternative 1\ndef factorialFuture(n: Int, accumulator: Int)" +
    ": Future[Int] = {\n  if (n == 1) Future.successful(accumulator)\n  else {\n    val result = factorialFuture(n -" +
    " 1, n * accumulator)\n    val finalResult = Await.result(result, 1000.millis)\n    Future.successful" +
    "(finalResult)\n  }\n}\n\n// Alternative 2\ndef factorialFutureAlt(n: Int, accumulator: Int): Future[Int] = {\n " +
    " if (n == 1) Future.successful(accumulator)\n  else Future(factorialFutureAlt(n - 1, n * accumulator)).flatMap" +
    "(identity _)\n}\n```\n\n")
  println("The second alternative is shorter, but needs the explicit creation of a future\nin the `if`-branch (for " +
    "lifting the `accumulator` variable), and the explicit\nintroduction of `flatMap` in each iteration, so that it " +
    "can flatten the nested\nfuture into a single future. We now run on of their implementations:\n\n")

  println(s"Running `factorialFuture(5, 1) = ${Await.result(factorialFuture(5, 1), 1000.millis)}`")

  scala.io.StdIn.readLine("\n\n################# Type return to continue ##############\n\n\n\n")

  println("With the usage of data-flow futures, the function can be written as follows:\n\n")

  println("```\nimport godot.imperative._\nimport godot.imperative.Flow._\nimport scala.concurrent.ExecutionContext" +
    ".Implicits.global\nimport scala.concurrent.duration._\nimport scala.concurrent.{Await, Future}\n\ndef factorial" +
    "(n: Int, accumulator: Int): Flow[Int] = {\n    if (n == 1) accumulator\n    else asyncS(factorial(n - 1, n * " +
    "accumulator))\n}\n```\n\n")
  println("The `if` branch automatically lifts the `accumulator` to a `Flow[Int]` and the\n`else` branch flattens " +
    "immediately the possible nested `Flow[Flow[Int]]`.\n\nWith the use of data-flow futures, we tackle the *The " +
    "Type Proliferation\nProblem*, as the data-flow futures hide their internal communication structure,\nand *The " +
    "Future Proliferation Problem*, as the data-flow future structure\nguarantees that there will be no ``falsely " +
    "fulfilled'' future (Section 4,\nIntegrating Data-Flow and Control-Flow Futures and Delegation).\n\n" +
    "This implementation partly solves *The Future Proliferation Problem*,\n" +
    "as it solves ``falsely fulfilling'' a future but does not introduce\n" +
    "the *forward* nor runs in constant space. This requires the use \n" +
    "of an advanced macro system or updating the Scala compiler, which is\n" +
    "outside of the scope of the artifact.\n\n")

  println(s"Running `factorial(5, 1) = ${factorial(5, 1).getS}`")

}

//object Features {
//  // Implicit lifting of any value to a data-flow future
//  def
//}