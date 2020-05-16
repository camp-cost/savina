package edu.rice.habanero.benchmarks.pingpong

import akka.actor.{ActorRef, Props}
import edu.rice.habanero.actors.{AkkaActor, AkkaActorState}
import edu.rice.habanero.benchmarks.pingpong.PingPongConfig.{Message, PingMessage, StartMessage, StopMessage}
import edu.rice.habanero.benchmarks.{Benchmark, BenchmarkRunner}

/**
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
object PingPongAkkaActorSeqCost {

  def main(args: Array[String]) {
    BenchmarkRunner.runBenchmark(args, new PingPongAkkaActorSeqCost)
  }

  private final class PingPongAkkaActorSeqCost extends Benchmark {
    def initialize(args: Array[String]) {
      PingPongConfig.parseArgs(args)
    }

    def printArgInfo() {
      PingPongConfig.printArgs()
    }

    def runIteration() {
          val system = AkkaActorState.newActorSystem("PingPong")
          var i = 0
          var tmp = 0.toDouble
          for (i <- 1 to 5) {
            tmp = tmp + scala.math.log(i.toDouble)
          }
          AkkaActorState.awaitTermination(system);
    }

    def cleanupIteration(lastIteration: Boolean, execTimeMillis: Double) {
    }
  }

}
