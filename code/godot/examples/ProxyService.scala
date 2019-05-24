import java.util.concurrent.atomic.AtomicInteger

import godot.imperative._
import godot.imperative.Flow._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent._

/**
  *
  * This is an example similar to the one explained in the paper, simulating the work of actors, using tasks instead.
  * The `Start` class is the main entry point. It creates a load balancer and "sends" a message to it. The load
  * balancer passes the work to a worker thread and returns the handle to this work, a data-flow future.
  * In the case of control-flow futures, the communication with the load balancer creates a future, which contains
  * another future, the one that the load balancer produces when it communicates with the worker thread.
  * With the use of data-flow futures, this does not happen.
  *
  */

// Costly computation
class Computation {
  def start(): Int = {
    42
  }
}

class Worker() {
  def run(job: Computation): Flow[Int] = asyncS {
    job.start()
  }
}

class LoadBalancer() {
  val workers = List(new Worker(), new Worker(), new Worker(), new Worker())
  val turn = new AtomicInteger(0)

  def run(job: Computation): Future[Flow[Int]] = {
    val currentTurn = turn.get()
    turn.weakCompareAndSet(currentTurn, (currentTurn + 1) % workers.size)
    Future(this.workers(currentTurn).run(job))
  }
}

object Main extends App {
  println("This program is similar to the one explained in the paper, simulating the work\nof actors, using tasks " +
    "instead.  The `Start` class is the main entry point. It\ncreates a load balancer and \"sends\" messages to it, " +
    "returning immediately\ncontrol-flow futures containing data-flow futures, i.e., Future[Flow[T]].  " +
    "The\noutermost future denotes whether the load balancer has found an idle worker and\nsuccessfully delegated the" +
    " job; the innermost (data-flow) future denotes the\nresult of `run(job)`. To make things simple, we will just " +
    "send jobs to idle\nworkers, map on the (control-flow) futures to get to their inner (data-flow) futures\nand " +
    "sum all their results, until we have a final single value.\n\n")

  println("```\nclass Start extends App {\n  val proxy = new LoadBalancer()\n  val listOfFlows = for (i <- 0 until " +
    "1000) {\n    list += proxy.run(new Computation())\n  }\n  val flowList = list.map(Await.result(_, 1000.millis))" +
    "\n  val actual = flowList.foldLeft(0)(_ + _.getS)\n  var list = new ListBuffer[Future[Flow[Int]]]()\n  println" +
    "(s\"Is the total value 42_000? ${actual == 42 * 1000}\")\n}\n```\n\n")

  scala.io.StdIn.readLine("\n\n################# Type return to continue ##############\n\n\n\n")

  println("The message (class `Computation`) is a ``costly'' computation:\n\n")

  println("```// Costly computation\nclass Computation {\n  def start(): Int = 42\n}\n```\n\n")

  println("The load balancer passes the work to a worker thread and returns the handle to\nthis work, a data-flow " +
    "future.\n\n")

  println("```\nclass LoadBalancer() {\n  val workers = List(new Worker(), new Worker(), new Worker(), new Worker())" +
    "\n  val turn = new AtomicInteger(0)\n\n  def run(job: Computation): Future[Flow[Int]] = {\n    val currentTurn " +
    "= turn.get()\n    turn.weakCompareAndSet(currentTurn, (currentTurn + 1) % workers.size)\n    Future(this" +
    ".workers(currentTurn).run(job))\n  }\n}\n\nclass Worker(){\n  def run(job: Computation): Flow[Int] = asyncS { " +
    "job.start() }\n}\n```\n\n")

  scala.io.StdIn.readLine("\n\n################# Type return to continue ##############\n\n\n\n")

  println("With the use of data-flow futures, we tackle the *The Type Proliferation\nProblem*, as the data-flow " +
    "futures hide their internal communication structure,\nand *The Fulfilment Observation Problem*, as it allows to" +
    " observe the current\nstage of futures computations, when necessary (Section 4, Integrating Data-Flow and" +
    "Control-Flow\nFutures and Delegation)\n\n")

  var list = new ListBuffer[Future[Flow[Int]]]()
  val proxy = new LoadBalancer()
  val listOfFlows = for (i <- 0 until 1000) {
    list += proxy.run(new Computation())
  }
  val flowList = list.map(Await.result(_, 1000.millis))
  val actual = flowList.foldLeft(0)(_ + _.getS)
  println(s"Running the sum of the asynchronous jobs: ${actual}  (check result = ${42 * 1000})\n\n")

  scala.io.StdIn.readLine("\n\n################# Type return to continue ##############\n\n\n\n")

  println("If we were to use control-flow futures, instead of data-flow futures, the\ncommunication with the load " +
    "balancer creates a future, which contains a nested\nfuture, the one that the load balancer produces when it " +
    "communicates with the\nworker thread; the return type would be `Future[Future[Int]]`. Any further\nasynchronous" +
    " call would be reflected in the types.")
}
