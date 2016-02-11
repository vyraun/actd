/**
 *
 */
package org.mmarini.actd

import breeze.linalg.DenseVector

/**
 * A learning agent that replies to stimulus with actions and learns by receiving rewards
 * applying TD algorithm.
 *
 * @constructor create a learning agent with parameters, critic network and actor network
 * @parm parms the parameters
 * @parm critic the critic network
 * @parm actor the actor network
 *
 * @author us00852
 */
class TDBatchAgent(
    val parms: TDParms,
    val critic: TDNeuralNet,
    val actor: TDNeuralNet,
    val buffer: Seq[Feedback]) extends Agent {

  /** Returns the action to be taken in a state */
  def action(status: Status): Action =
    parms.indexEGreedyBySoftmax(actor(status.toDenseVector).output)

  /** Returns a new agent that learns by reward */
  private def trainCritic(feedback: Feedback): (TDNeuralNet, Seq[Feedback]) = {
    // Creates new buffer
    val nb = (buffer :+ feedback).takeRight(parms.maxTrainingSample)

    def trainSample(net: TDNeuralNet, feedback: Feedback): TDNeuralNet = {

      // Computes the state value pre and post step
      val s0Vect = feedback.s0.toDenseVector
      val s1Vect = feedback.s1.toDenseVector

      val end0 = feedback.s0.finalStatus
      val end1 = feedback.s1.finalStatus

      // The status value of post state is 0 if final episode else bootstraps from critic
      val postValue = if (end1 || end0) 0.0 else net(s1Vect).output(0)

      // Computes the expected state value by booting the previous status value */
      val expectedValue = postValue * parms.gamma + feedback.reward

      // Computes the error by critic
      val preValue = net(s0Vect).output(0)

      // Teaches the critic by evidence
      net.learn(s0Vect, DenseVector(expectedValue)).clearTraces
    }

    def trainLoop(net: TDNeuralNet, i: Int): TDNeuralNet = {
      nb.foldLeft(net)(trainSample)
    }

    // Trains new critic
    val nc = (1 to parms.maxTrainingIteration).foldLeft(critic)(trainLoop)

    // Creates new agent
    (nc, nb)
  }

  /** Returns a new agent that has learned by reward and the error */
  def train(feedback: Feedback): (TDBatchAgent, Double) = {

    val (nc, nb) = trainCritic(feedback)

    // Computes the state value pre and post step
    val s0Vect = feedback.s0.toDenseVector
    val s1Vect = feedback.s1.toDenseVector

    val end0 = feedback.s0.finalStatus
    val end1 = feedback.s1.finalStatus

    // The status value of post state is 0 if final episode else bootstraps from critic
    val postValue = if (end1 || end0) 0.0 else nc(s1Vect).output(0)

    // Computes the expected state value by booting the previous status value */
    val expectedValue = postValue * parms.gamma + feedback.reward

    // Computes the error by critic
    val preValue = nc(s0Vect).output(0)
    val delta = expectedValue - preValue

    // Computes the expected action preferences applying the critic error to previous decision */
    val pref = actor(s0Vect).output
    val expectedPref = pref.copy
    val action = feedback.action
    expectedPref(action to action) += parms.beta * delta

    // Teaches the actor by evidence
    val na = actor.learn(s0Vect, expectedPref)

    val nag = if (end0) {
      new TDBatchAgent(parms, nc.clearTraces, na.clearTraces, nb)
    } else {
      new TDBatchAgent(parms, nc, na, nb)
    }
    (nag, delta)
  }

}

/** Factory for [[TDAgent]] instances */
object TDBatchAgent {

  /**
   * Creates a TDAgent with TD parameter,
   *  hidden layers networks and
   *  weights within a range.
   */
  def apply(
    parms: TDParms,
    sigma: Double,
    statusSize: Int,
    actionCount: Int,
    hiddenLayers: Int*): TDBatchAgent =
    new TDBatchAgent(parms,
      TDNeuralNet(statusSize +: hiddenLayers :+ 1, parms, sigma),
      TDNeuralNet(statusSize +: hiddenLayers :+ actionCount, parms, sigma),
      Seq())
}
