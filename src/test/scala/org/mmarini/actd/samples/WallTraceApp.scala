/**
 *
 */
package org.mmarini.actd.samples

import java.io.File

import org.apache.commons.math3.random.MersenneTwister
import org.mmarini.actd.Environment
import org.mmarini.actd.Feedback
import org.mmarini.actd.TDAgent
import org.mmarini.actd.TDParms
import com.typesafe.scalalogging.LazyLogging
import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector
import breeze.linalg.csvwrite
import breeze.stats.distributions.RandBasis
import scala.math.pow

/**
 * Tests the maze environment
 * and generates a report of episode returns as octave data file
 */
object WallTraceApp extends App with LazyLogging {

  val file = "data/wall.csv"
  val StepCount = 1000000
  val SampleTraceCount = 1000

  val initEnv = WallStatus.environment

  /** Generates the report */
  private def report: Stream[DenseVector[Double]] = {
    val s1 = WallTestStreams.toSamplesWithAC(WallStatus.environment.toStream)
    val s2 = TestStreams.trace(s1, "Sample", SampleTraceCount)
    s2.take(StepCount)
  }

  TestStreams.write(file, report)
}
