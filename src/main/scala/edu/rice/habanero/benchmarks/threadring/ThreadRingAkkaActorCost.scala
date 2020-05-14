package edu.rice.habanero.benchmarks.threadring

import akka.actor.{ActorRef, Props}
import edu.rice.habanero.actors.{AkkaActor, AkkaActorState}
import edu.rice.habanero.benchmarks.threadring.ThreadRingConfig.{DataMessage, ExitMessage, PingMessage}
import edu.rice.habanero.benchmarks.{Benchmark, BenchmarkRunner}

/**
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
object ThreadRingAkkaActorCost{

  def main(args: Array[String]) {
    BenchmarkRunner.runBenchmark(args, new ThreadRingAkkaActorCost)
  }

  private final class ThreadRingAkkaActorCost extends Benchmark {
    def initialize(args: Array[String]) {
      ThreadRingConfig.parseArgs(args)
    }

    def printArgInfo() {
      ThreadRingConfig.printArgs()
    }

    def runIteration() {

      val system = AkkaActorState.newActorSystem("ThreadRing")

      val numActorsInRing = ThreadRingConfig.N
      val ringActors = Array.tabulate[ActorRef](numActorsInRing)(i => {
        val loopActor = system.actorOf(Props(new ThreadRingActor(i, numActorsInRing)))
        AkkaActorState.startActor(loopActor)
        loopActor
      })

      for ((loopActor, i) <- ringActors.view.zipWithIndex) {
        val nextActor = ringActors((i + 1) % numActorsInRing)
        loopActor ! new DataMessage(nextActor)
      }

      // Changed from original implementation to ensure a fixed number of
      // iterations 'R''
      ringActors(0) ! new PingMessage(ThreadRingConfig.R * ThreadRingConfig.N)

      AkkaActorState.awaitTermination(system)
    }

    def cleanupIteration(lastIteration: Boolean, execTimeMillis: Double) {
    }
  }

  private class ThreadRingActor(id: Int, numActorsInRing: Int) extends AkkaActor[AnyRef] {

    private var nextActor: ActorRef = null

    override def process(msg: AnyRef) {

      msg match {

        case pm: PingMessage =>

          // Artificial computation to add some delay
          // simulates some larger computation used 
          var i = 0
          var tmp = 0.toDouble
          for (i <- 1 to 5) {
            tmp = tmp + scala.math.log(i.toDouble)
          }

          if (pm.hasNext) {
            nextActor ! pm.next()
          } else {
            nextActor ! new ExitMessage(numActorsInRing)
          }

        case em: ExitMessage =>

          if (em.hasNext) {
            nextActor ! em.next()
          }
          exit()

        case dm: DataMessage =>

          nextActor = dm.data.asInstanceOf[ActorRef]
      }
    }
  }

}

object AsyncThreadRingAkkaActorCost{

  def main(args: Array[String]) {
    BenchmarkRunner.runBenchmark(args, new AsyncThreadRingAkkaActorCost)
  }

  private final class AsyncThreadRingAkkaActorCost extends Benchmark {
    def initialize(args: Array[String]) {
      ThreadRingConfig.parseArgs(args)
    }

    def printArgInfo() {
      ThreadRingConfig.printArgs()
    }

    def runIteration() {

      val system = AkkaActorState.newActorSystem("ThreadRing")

      val numActorsInRing = ThreadRingConfig.N
      val ringActors = Array.tabulate[ActorRef](numActorsInRing)(i => {
        val loopActor = system.actorOf(Props(new AsyncThreadRingActor(i, numActorsInRing)))
        AkkaActorState.startActor(loopActor)
        loopActor
      })

      for ((loopActor, i) <- ringActors.view.zipWithIndex) {
        val nextActor = ringActors((i + 1) % numActorsInRing)
        loopActor ! new DataMessage(nextActor)
      }

      for (loopActor <- ringActors) {
        loopActor ! new PingMessage(ThreadRingConfig.R)
      }

      AkkaActorState.awaitTermination(system)
    }

    def cleanupIteration(lastIteration: Boolean, execTimeMillis: Double) {
    }
  }

  private class AsyncThreadRingActor(id: Int, numActorsInRing: Int) extends AkkaActor[AnyRef] {

    private var nextActor: ActorRef = null
    private var iters = ThreadRingConfig.R

    override def process(msg: AnyRef) {

      msg match {

        case pm: PingMessage =>

          if (iters > 0) {
            nextActor ! new PingMessage(iters)
            iters = iters - 1

            // Artificial computation to add some delay
            // simulates some larger computation used 
            var i = 0
            var tmp = 0.toDouble
            for (i <- 1 to 5) {
              tmp = tmp + scala.math.log(i.toDouble)
            }
          } else {
            nextActor ! new ExitMessage(numActorsInRing)
          }

        case em: ExitMessage =>

          exit()
        
        case dm : DataMessage =>
          nextActor = dm.data.asInstanceOf[ActorRef]
      }
    }
  }
}