package godot.imperative

import godot.imperative.Flow._
import junit.framework.TestCase
import org.junit.Assert._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class LiftingTest extends TestCase {
  val FLOW_INNER_VALUE: Int = 42
  val FLOW_INNER_FUT_VALUE: Int = 1
  val thisFut: Future[Int] = Future(FLOW_INNER_FUT_VALUE)
  var flowValue: Flow[Int] = FLOW_INNER_VALUE
  var flowFuture: Flow[Int] = lift(thisFut)
  var flowToFuture: Flow[Future[_]] = thisFut

  object Utils {
    def methodThatImplicitlyLiftsValue[T](x: Flow[T]) = x
  }

  def testLiftingFutureValueReturnsFlow(): Unit = {
    assertEquals(getS(flowFuture), getS(lift(thisFut)))
  }

  def testLiftingAFutFlowReturnsASingleFlow(): Unit = {
    Future(34).flatMap((x: Int) => Future(x))
    assertEquals(42, getS(lift(Future(asyncS(42)))))
  }

  def testImplicitLiftingOfValueToFlow(): Unit = {
    assertEquals(flowValue.getS, Utils.methodThatImplicitlyLiftsValue(FLOW_INNER_VALUE).getS)
  }

  def testImplicitLiftingOfFutureValueToFlow(): Unit = {
    assertEquals(flowToFuture.getS, Utils.methodThatImplicitlyLiftsValue(thisFut).getS)
  }
}
