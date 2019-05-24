package godot.imperative

import godot.imperative.Flow._
import junit.framework.TestCase
import org.junit.Assert._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class MonadicOperations extends TestCase{
  val FLOW_INNER_VALUE: Int = 42
  val FLOW_INNER_FUT_VALUE: Int = 1
  val thisFut: Future[Int] = Future(FLOW_INNER_FUT_VALUE)
  var flowValue: Flow[Int] = FLOW_INNER_VALUE
  var flowFuture: Flow[Int] = lift(thisFut)
  var flowToFuture: Flow[Future[_]] = thisFut

  def testFlatMapOfNestedFlows(): Unit = {
    val result = asyncS(asyncS(asyncS(FLOW_INNER_VALUE))).flatMap((x: Int) => x)
    assertEquals(FLOW_INNER_VALUE, result.getS)
  }

  def testFlatMapOfFutureFlow(): Unit = {
    val result = asyncS(asyncS(asyncS(thisFut))).flatMap((x: Future[Int]) => x)
    assertEquals(thisFut, result.getS)
  }

  def testMappingMultipleTimes(): Unit = {
    val result = FLOW_INNER_VALUE.map((x: Int ) => x % 2)
      .flatMap((x: Int) => asyncS(x + FLOW_INNER_VALUE))
      .map((x: Int) => x.toString)
    assertEquals(((FLOW_INNER_VALUE % 2) + FLOW_INNER_VALUE).toString, result.getS)
  }

  def testMapsInTermsOfFor(): Unit = {
    val result = for {
      flowInt <- asyncS(FLOW_INNER_VALUE)
      flowString <- flowInt.map((x : Int) => x.toString())
    } yield flowString
    assertEquals(FLOW_INNER_VALUE.toString, result.getS)
  }

  def testLeftIdentity(): Unit = {
    // return a >>= f   equiv  f a
    val actual = asyncS { FLOW_INNER_VALUE }.flatMap((x: Int) => asyncS(x.toString))
    val expected = ((x: Int) => asyncS(x.toString))(FLOW_INNER_VALUE)
    assertEquals(expected.getS, actual.getS)
  }

  def testRightIdentity(): Unit = {
    // m >>= return   equiv  m
    val actual = asyncS(FLOW_INNER_VALUE).flatMap((x: Int) => asyncS{x})
    val expected = asyncS{FLOW_INNER_VALUE}
    assertEquals(expected.getS, actual.getS)
  }

  def testAssociativity(): Unit = {
    // (m >>= f) >>= g   equiv   m >>= (\x -> f x >>= g)
    val expected = (asyncS(FLOW_INNER_VALUE).flatMap((x: Int) => asyncS{x + 1})).flatMap((x: Int) => asyncS{x + 1})
    val actual = asyncS(FLOW_INNER_VALUE).flatMap((x: Int) => asyncS{x + 1}.flatMap((x: Int) => asyncS{x + 1}))
    assertEquals(expected.getS, actual.getS)
  }

}
