package godot.imperative


import scala.language.implicitConversions
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import scala.language.postfixOps

/*** Data-flow future implementation
  *
  * Data-flow futures have been implemented using a singleton object and trait. This is a more object-oriented approach
  * than the calculus from the paper, but it fits better in the Scala ecosystem. We use case classes `Value`
  * and `Diamond` to extend the `Flow` trait and to encode whether non-future values are lifted to data-flow futures
  * or whether future values are lifted to flows. This encoding allows one to write the following code (which
  * resembles programming with `Futures` in Scala):
  *
  *   ```
  *   Flow {  computation()  }.getS
  *   ```
  *
  * One can also allow to stick to the functional notation form the paper, so that you can write your code as follows:
  *
  *   ```
  *   getS(  asyncS { computation()  } )
  *   ```
  *
  * The pattern matching from the paper has been encoded using `match` in Scala and closely follows the calculus,
  * except that we polymorphically dispatch to the appropriate case class. The implicit lifting from the paper is
  * encoded using implicit functions in Scala (functions `liftToFlow`).
  *
  * There is one point where we differ from the paper, which has to do with the `R-FlowCompression` (page 21), which
  * affects the encoding of the `thenS` combinator (in the implementation this is named `map`, and is due to
  * not being able to handle implicit delegation (Section 8.1), discussed in Section 8.2 Notes on Implementing Godot.
  *
  * In this implementation, the deviation is reflected in `flatMap` in the `Diamond` case class (the `Value` case class
  * presents no issues). The deviation basically says that if one tries to perform `thenS(flow, fun: T => Flow[T])`,
  * where the flow is a `Diamond(fut)`, then we perform a future chaining operation that flattens the returned
  * `Future` (we perform a `flatMap` operation instead of a `map` operation). If the result of applying the
  * function to the value from the flow is a lifted, non-flow value, we lift it to a Diamond; if it is a
  * lifted control-flow future, then we just return it.
  *
  */


object Flow {
  /** * Implicit lifting of value to flow type
    *
    * @param x the value to be lifted to a flow type
    * @tparam T generic type of the value
    * @return a flow type
    */
  implicit def liftToFlow[T](x: T)(implicit ec: ExecutionContext): Flow[T] = Value(x)
  implicit def liftToFlow[T](x: Flow[T])(implicit ec: ExecutionContext, dummyImplicit: DummyImplicit): Flow[T] = x

  /** * Get the value out of a flow type
    *
    * @param x a flow type
    * @tparam T generic type inside the flow type, where T != Flow[R]
    * @return the value inside the flow type
    */
  def getS[T](x: Flow[T]): T = x.getS

  /** * Applies the specified function to the value inside the flow, and returns
    * a new flow with the result of this application.
    *
    * @param flow the flow abstraction
    * @param fun  the function to apply to the flow
    * @param ec   the implicit execution context
    * @tparam T the type of the value inside the flow abstraction
    * @tparam U the resulting type of the returned function
    * @return a flow type
    */
  def flatMap[T, U](flow: Flow[T], fun: T => Flow[U])(implicit ec: ExecutionContext): Flow[U] = flow.flatMap(fun)
  def map[T,U: Promise](flow: Flow[T], fun: T => Flow[U])(implicit ec: ExecutionContext): Flow[U] = flow.flatMap(fun)
  def map[T,U](flow: Flow[T], fun: T => U)(implicit ec: ExecutionContext): Flow[U] = flow.map(fun)


  /*** Creates a data-flow future.
    *
    * This implementation is the same as calling the `asynccS` function, except that it is more consistent with the
    * `Future` way of spawning computations
    *
    * @param body expression to run asynchronously
    * @param ec execution context
    * @tparam T type variable inside the data-flow future
    * @return returns an asynchronous data-flow computation
    */
  def apply[T](body: => Flow[T])(implicit ec: ExecutionContext): Flow[T] = asyncS(body)
  def apply[T](body: => T, dummyImplicit: DummyImplicit)(implicit ec: ExecutionContext): Flow[T] = asyncS(body)

  /** * Lifts a computation to a flow computation.
    *
    * @param body expression to run asynchronously
    * @tparam T the generic type inside the flow type, where T != Flow[R]
    * @return the value inside the flow type
    */
  def asyncS[T](body: => Flow[T])(implicit ec: ExecutionContext): Flow[T] = {
    body match {
      case Value(v) => Diamond(Future(v))
      case Diamond(fut) => Diamond(fut.map(identity))
    }
  }

  /** * Lifts a computation with type T != FlowTC[S] to a flow computation.
    *
    * @param body expression to run asynchronously
    * @param ec   execution context
    * @tparam T returned type from the body parameter, where T != Flow[R]
    * @return a flow type that contains the result of running the body
    *         expression
    */
  def asyncS[T](body: => T)(implicit ec: ExecutionContext, dummyImplicit: DummyImplicit): Flow[T] = Value(body)

  /** * Lifts a future to a flow type.
    *
    * NOTE: alternatively, use type constraints such as (implicit evidence: T =:= FlowTC[_])
    *
    * @param x future value to be lifted to a flow type
    * @tparam T generic type of the future
    * @return flow type
    */
  def lift[T](x: Future[T])(implicit ec: ExecutionContext): Flow[T] = Diamond(x)

  def lift[T](x: Future[Flow[T]])(implicit ec: ExecutionContext, dummyImplicit: DummyImplicit): Flow[T] = {
    val p = Promise[T]()
    x.map((flow : Flow[T]) =>  flow.map((result: T) => { p.success(result) }))
    lift(p.future)
  }
}

/*** Flow trait
  *
  * Flow has been implemented as a trait, so that we can distinguished between non-future values lifted to flows and
  * future values lifted to flows. This encoding is more object-oriented than what we explain in the paper, but makes
  * easier to write programs, as developers can write:
  *
  *   ```
  *   Flow {  computation()  }.getS
  *   ```
  *
  * @tparam T Type parameter
  */
trait Flow[T] {
  def getS: T
  def flatMap[U](fun: T => Flow[U])(implicit ec: ExecutionContext): Flow[U]
  def map[U](fun: T => U)(implicit ec: ExecutionContext): Flow[U]
  def map[U](fun: T => Flow[U])(implicit ec: ExecutionContext, dummyImplicit: DummyImplicit): Flow[U]
}

/*** Case class used to mimick implicitly lifted values
  *
  * @param v Value that represents a data-flow future
  * @tparam T Type parameter
  */
private case class Value[T](v: T) extends Flow[T]{
  override def getS: T = v

  override def flatMap[U](fun: T => Flow[U])(implicit ec: ExecutionContext): Flow[U] = fun(v)

  override def map[U](fun: T => U)(implicit ec: ExecutionContext): Flow[U] = Value(fun(v))

  override def map[U](fun: T => Flow[U])(implicit ec: ExecutionContext, dummyImplicit: DummyImplicit): Flow[U] = flatMap(fun)
}

/*** Case class that represents the Diamond operation from the paper, i.e., a lifted future
  *
  * @param fut Future lifted to a data-flow future
  * @tparam T Type parameter
  */
private case class Diamond[T](fut: Future[T]) extends Flow[T] {
  override def getS: T = Await.result(fut, 5000 millis)

  override def flatMap[U](fun: T => Flow[U])(implicit ec: ExecutionContext): Flow[U] = {
    val newFut = fut.flatMap[U]((x: T) =>
      (fun(x) : Flow[U]) match {
      case Value(v) => Future.successful(v)
      case Diamond(innerFut : Future[U]) => innerFut
    })
    Diamond(newFut)
  }

  override def map[U](fun: T => U)(implicit ec: ExecutionContext): Flow[U] = Diamond(fut.map((x: T) => fun(x)))
  override def map[U](fun: T => Flow[U])(implicit ec: ExecutionContext, dummyImplicit: DummyImplicit): Flow[U] = flatMap(fun)
}
