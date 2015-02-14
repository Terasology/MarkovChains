/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.markovChains;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import org.terasology.markovChains.dataStructures.TransitionMatrix;

import java.util.Collection;

/**
 * N-order Markov Chain implementation.
 * This is the raw version with a low level interface.
 * Directed usage of this class should be avoided;
 * use the {@link MarkovChain} unless low level control is necessary.
 *
 * As opposed to the {@link MarkovChain} class:
 *  * users have manually to provide a random number and state history to getNext() methods manually.
 *  * users have to make sure that the object is in a normalized state before calling getNext().
 *  * states are represented as integer values.
 *
 * @since 1.00
 * @version 1.50
 * @author Linus van Elswijk
 */
public class RawMarkovChain extends MarkovChainBase {

    private final TransitionMatrix transitionMatrix;

    // public //////////////////////////////////////////////////////////

    /**
     * Constructs a Markov Chain of any order and any nr of states.
     *
     * The constructed object will be in a state where isNormalized() returns false.
     *
     * <b>Note:</b> Avoid calling this constructor directly. Use the more user friendly, 2D and 3D array versions
     * whenever possible.
     *
     * @param transitionMatrix The transition probabilities of length pow(nrOfStates, order + 1).
     *      The provided array should be a flattened n-dimensional array of probabilities, with
     *      n being the order of the markov chain.
     *
     * @since 1.50
     */
    public RawMarkovChain(final TransitionMatrix transitionMatrix) {
        super(transitionMatrix.getOrder(), transitionMatrix.getNrOfStates());

        this.transitionMatrix = transitionMatrix;
    }

    /**
     * Returns the next state, given a random number and state history.
     * @param randomNumber a random number in the range [0, 1).
     * @param states The history of states, from least recent to most recent.
     *               The amount of states given as history should equal the order of the Markov Chain.
     * @return The next state
     *
     * @since 1.00
     */
    public int getNext(float randomNumber, Collection<Integer> states) {
        return getNext(randomNumber, Ints.toArray(states));
    }

    /**
     * Returns the next state, given a random number and state history.
     * @param randomNumber a random number in the range [0, 1).
     * @param states The history of states, from least recent to most recent.
     *               The amount of states given as history should equal the order of the Markov Chain.
     * @return The next state
     *
     * @since 1.00
     */
    public int getNext(final float randomNumber, final int ... states) {
        // check preconditions //////////////////////
        // error messages ///////////////////////
        final String randomNumberOutOfRangeFormat =
                "randomNumber = %s; must be a nr >= 0 and < 1.0";

        final String notNormalizedMessage =
                "Object has not been normalized";

        // checks //////////////////////////////

        Preconditions.checkArgument(
                0 <= randomNumber && randomNumber < 1.0,
                randomNumberOutOfRangeFormat,
                randomNumber
        );

        transitionMatrix.checkInputStates(true, 1, states);

        Preconditions.checkState(
                transitionMatrix.isNormalized(),
                notNormalizedMessage
        );

        // method body /////////////////////////////

        int i;
        float leftOver = randomNumber;
        float[] transitionsProbabilities = transitionMatrix.getRow(states);

        for (i = 0; i < transitionsProbabilities.length; i++) {
            leftOver -= transitionsProbabilities[i];
            if (leftOver < 0) {
                return i;
            }
        }

        /* NOTE:
         * There's a very tiny possibility that transitionsProbabilities[i] == 0f,
         * because of numerical instability.
         * Therefore we roll back to the first non zero element in the array.
         * The normalization process makes sure that there is at least one such element, before
         * i == 0.
         */
        while (transitionsProbabilities[i] == 0) {
            i--;
        }

        return i;
    }

    /**
     * Returns (a reference to) the transition matrix of the MarkovChain
     * @return the transition matrix
     */
    public TransitionMatrix getTransitionMatrix() {
        return this.transitionMatrix;
    }
}
