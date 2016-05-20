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

import org.mmarini.actd.EnvironmentActor
import org.mmarini.actd.ACAgent

import com.typesafe.scalalogging.LazyLogging

/**
 * Tests the maze environment
 * and generates a report of episode returns as octave data file
 */
object FilteredWallTraceApp extends App with WallEnvironment with AgentSave with LazyLogging {

  val States = Set[FlatWallStatus]()

  val processorActorsSet = Set(saveActors)

  val environment =
    system.actorOf(
      EnvironmentActor.props(FlatWallStatus.initStatus, FlatWallStatus.initAgent(WallArguments(args))))

  val controllerActor = system.actorOf(TakeUntilActor.props(environment, {
    (f, d, a) => f.s1.finalStatus
  }))

  startSim

  waitForCompletion
}

//  /*
//     * Filter on the following status
//     *
//     *   8  O
//     *   9   o
//     *  10    o---
//     *      234567
//     */
//  private def filter2(x: DenseVector[Double]) =
//    x(RowIdx) == 8 && x(ColIdx) == 2 && x(RowSpeedIdx) == 1 && x(ColSpeedIdx) == 1 && x(PadIdx) == 5
//
//  /** Generates the report */
//  //  WallStatus.environment.iterator.
//  //    toSamplesWithAC.
//  //    trace("Sample", SampleTraceCount).
//  //    filter(filter).
//  //    trace("Filtered").
//  //    take(EpisodeCount).
//  //    write(file)
//}
