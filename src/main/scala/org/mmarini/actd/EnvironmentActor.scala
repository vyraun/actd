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

package org.mmarini.actd

import org.mmarini.actd.TDAgentActor.Feed
import org.mmarini.actd.TDAgentActor.React
import org.mmarini.actd.TDAgentActor.Reaction
import org.mmarini.actd.TDAgentActor.Trained

import EnvironmentActor.Step
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

/** Props and messages factory for [[EnvironmentActor]] */
object EnvironmentActor {

  /**
   * Creates the props for a [[TDAgentActor]]
   *
   * @param initStatus initial status
   * @param critic the initial critic network
   * @param actor the initial actor network
   *
   */
  def props(initStatus: Status,
    parms: TDParms,
    critic: TDNeuralNet,
    actor: TDNeuralNet): Props =
    Props(classOf[EnvironmentActor], initStatus, parms, critic, actor)

  /** Message to [[EnvironmentActor]] to process a single step interaction */
  object Interact

  /** Message by [[EnvironmentActor]] to reply a [[Next]] */
  case class Step(feedback: Feedback, delta: Double, critic: TDNeuralNet, actor: TDNeuralNet)
}

/**
 * An Actor that does the interaction between the environment and the agent
 *
 * @constructor create the actor
 * @param initStatus initial status
 * @param critic the initial critic network
 * @param actor the initial actor network
 */
class EnvironmentActor(initStatus: Status,
    parms: TDParms,
    critic: TDNeuralNet,
    actor: TDNeuralNet) extends Actor with ActorLogging {

  import EnvironmentActor._

  val agent = context.actorOf(TDAgentActor.props(parms, critic, actor))

  def receive: Receive = waiting(initStatus)

  /** Processes the messages while is waiting for an Interact request */
  private def waiting(status: Status): Receive = {
    case Interact =>
      context become waitingReaction(status, sender)
      agent ! React(status)
  }

  /** Processes the messages while is processing an Interact request */
  private def waitingReaction(
    status: Status,
    replyTo: ActorRef): Receive = {

    case Reaction(action) =>
      val f = status(action)
      agent ! Feed(f)
      context become waitingTrained(f.s1, replyTo, f)
  }

  /** Processes the messages while is processing an Interact request */
  private def waitingTrained(
    status: Status,
    replyTo: ActorRef,
    feedback: Feedback): Receive = {

    case Trained(delta, critic, actor) =>
      replyTo ! Step(feedback, delta, critic, actor)
      context become waiting(status)
  }
}
