// Copyright (c) 2016 Marco Marini, marco.marini@mmarini.org
//
// Licensed under the MIT License (MIT);
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://opensource.org/licenses/MIT
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.

package org.mmarini.actd.samples

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import org.mmarini.actd.EnvironmentActor
import com.typesafe.scalalogging.LazyLogging
import akka.actor.ActorSystem
import akka.pattern.{ gracefulStop, ask }
import org.mmarini.actd.Feedback
import akka.util.Timeout
import org.mmarini.actd.TDNeuralNet

/**
 * Tests the maze environment
 * and generates a report of episode returns as octave data file
 */
object WallTraceApp extends App with LazyLogging {
  val File = "data/wall.csv"
  val StepCount = 1000
  val system = ActorSystem("WallTraceApp")

  val (initStatus, parms, critic, actor) = WallStatus.initEnvParms

  val environment = system.actorOf(
    EnvironmentActor.props(initStatus, parms, critic, actor))

  val takeActor = system.actorOf(TakeActor.props(environment, StepCount))

  implicit val timeout = Timeout(5 seconds)

  val f = (takeActor ask None).mapTo[Seq[(Feedback, Double, TDNeuralNet, TDNeuralNet)]]

  Await.result(f, 100 seconds).
    iterator.
    toSamples.
    write(File)

  system stop environment

  system.terminate
}
