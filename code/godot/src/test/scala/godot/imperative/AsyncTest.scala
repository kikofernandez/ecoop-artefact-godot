package godot.imperative


import godot.imperative.Flow._
import junit.framework.TestCase
import org.junit.Assert._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class AsyncTest extends TestCase{
  val FLOW_INNER_VALUE: Int = 42
  val FLOW_INNER_FUT_VALUE: Int = 1
  val thisFut: Future[Int] = Future(FLOW_INNER_FUT_VALUE)
  var flowValue: Flow[Int] = FLOW_INNER_VALUE
  var flowFuture: Flow[Int] = lift(thisFut)
  var flowToFuture: Flow[Future[_]] = thisFut

  def testAsyncValueReturnsFlow(): Unit = {
    assertEquals(getS(flowFuture),
      getS(asyncS(FLOW_INNER_FUT_VALUE)))
  }

  def testAsyncFlowReturnsFlow(): Unit = {
    assertEquals(getS(flowFuture), getS(asyncS(FLOW_INNER_FUT_VALUE)))
  }

  def testAsyncFutureReturnsFlowToFuture(): Unit = {
    assertEquals(thisFut, getS(asyncS(thisFut)))
  }
}