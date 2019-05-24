package godot.imperative

import godot.imperative.Flow._
import junit.framework.TestCase
import org.junit.Assert._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class BlockingTest extends TestCase{

  val FLOW_INNER_VALUE: Int = 42
  val FLOW_INNER_FUT_VALUE: Int = 1
  val thisFut: Future[Int] = Future(FLOW_INNER_FUT_VALUE)
  var flowValue: Flow[Int] = FLOW_INNER_VALUE
  var flowFuture: Flow[Int] = lift(thisFut)
  var flowToFuture: Flow[Future[_]] = thisFut



  def testGetAFlowValue(): Unit = {
    assertEquals(FLOW_INNER_VALUE, getS(flowValue))
  }

  def testGetAFlowValueFromALiftedFuture(): Unit = {
    assertEquals(FLOW_INNER_FUT_VALUE, getS(flowFuture))
  }

  def testBlockingAfterMultipleFlows(): Unit = {
    val value = asyncS(asyncS(asyncS(asyncS(FLOW_INNER_VALUE))))
    assertEquals(FLOW_INNER_VALUE, getS(value))
  }

  def testCreateNonFinalAsyncComputation(): Unit = {
    val (expected, computation) = expressionThatPerformsBunchOfAsyncs()
    assertEquals(expected, computation)
  }

  private def expressionThatPerformsBunchOfAsyncs(): (Int, Int) = {
    import Flow._
    val NUMBER1 = 42
    val NUMBER2 = 100
    val flow1 = asyncS(NUMBER1)
    val flow2 = lift(thisFut)
    val flow3 = asyncS(asyncS(NUMBER2))
    (FLOW_INNER_FUT_VALUE + NUMBER1 + NUMBER2, getS(flow1) + getS(flow2) + getS(flow3))
  }
}

