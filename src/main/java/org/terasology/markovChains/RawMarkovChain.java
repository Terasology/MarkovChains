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
import org.terasology.math.TeraMath;

import java.util.Arrays;
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
 * @version 1.01
 * @author Linus van Elswijk
 */
public class RawMarkovChain extends MarkovChainBase {

    // variables ///////////////////////////////////////////////////////
    private static final int NO_INVALID_STATES = -1;

    private boolean isNormalized;
    private final float[] transitionProbabilityArray;

    // public //////////////////////////////////////////////////////////

    /**
     * Constructs a second order Markov Chain.
     *
     * The constructed object will be in a state where isNormalized() returns false.
     *
     * @param transitionMatrix A 3d matrix of transition probabilities.
     *                      Every element probabilities[x][y][z] determines the probability of transitioning
     *                      to state z, given previous state y and second previous state x.
     *                      The matrix must be cubical.
     *
     * @since 1.00
     */
    public RawMarkovChain(final float[][][] transitionMatrix) {
        this(2, transitionMatrix[0][0].length, flatten(transitionMatrix));
    }

    /**
     * Constructs a (first order) Markov Chain.
     *
     * The constructed object will be in a state where isNormalized() returns false.
     *
     * @param probabilities A 2d matrix of transition probabilities.
     *                      Every element probabilities[x][y] determines the probability of transitioning
     *                      from state x to state y.
     *                      The matrix must be square.
     *
     * @since 1.00
     */
    public RawMarkovChain(final float[][] probabilities) {
        this(1, probabilities[0].length, flatten(probabilities));
    }

    /**
     * Constructs a Markov Chain of any order and any nr of states.
     *
     * The constructed object will be in a state where isNormalized() returns false.
     *
     * <b>Note:</b> Avoid calling this constructor directly. Use the more user friendly, 2D and 3D array versions
     * whenever possible.
     *
     * @param order The order (>= 1) of the Markov Chain,
     *      i.e. how many (previous) states are considered to compute the next.
     * @param nrOfStates The nr of states (>=1).
     * @param probabilities The transition probabilities of length pow(nrOfStates, order + 1).
     *      The provided array should be a flattened n-dimensional array of probabilities, with
     *      n being the order of the markov chain.
     *
     * @since 1.00
     */
    public RawMarkovChain(final int order, final int nrOfStates, final float[] probabilities) {
        super(order, nrOfStates);

        // argument exception message formats //////////////////////////////////////

        final String probabilitiesArgumentExceptionFormat =
                "probabilities.length=%s, with order=%s and nrOfStates=%s the expected length is %s";
        final String invalidProbabilityExceptionFormat =
                "Invalid probability value: probabilities[%s] = %s";

        // check preconditions /////////////////////

        for (int i = 0; i < probabilities.length; i++) {
            Preconditions.checkArgument(
                    probabilities[i] >= 0f,
                    invalidProbabilityExceptionFormat,
                    i, probabilities[i]
            );
        }

        // check argument preconditions  ///////////////////////////////////////////

        final int requiredNrOfProbabilities = TeraMath.pow(this.nrOfStates, this.order + 1);

        Preconditions.checkArgument(
                probabilities.length == requiredNrOfProbabilities,
                probabilitiesArgumentExceptionFormat,
                probabilities.length, order, nrOfStates, requiredNrOfProbabilities
        );

        // initialize private data ////////////////////////////////////////////////

        transitionProbabilityArray = Arrays.copyOf(probabilities, requiredNrOfProbabilities);
        isNormalized = false;
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

        checkInputStates(true, 1, states);

        Preconditions.checkState(
                isNormalized,
                notNormalizedMessage
        );

        // method body /////////////////////////////
        final int startIndex = toIndex(states);
        final int endIndex = lastIndex(states);

        int i;
        float leftOver = randomNumber;
        for (i = startIndex; i < endIndex; i++) {
            leftOver -= transitionProbabilityArray[i];
            if (leftOver < 0) {
                return i % nrOfStates;
            }
        }

        return i % nrOfStates;

        /* NOTE:
         * There's a tiny possibility that transitionProbabilityArray[i] == 0f,
         * because of numerical instability.
         * Therefore we roll back to the first non zero element in the array.
         * The normalization process makes sure that there is at least one such element, before
         * i == startIndex.
         */
    }

    /**
     * Returns the probability of transitioning to a state X_n, given history X_0 .. X_n-1
     * @param states The history (X_0 .. X_n-1) states and target state X_n in the order X_0 to X_n.
     *               The amount of states given as history should equal the order of the Markov Chain.
     * @return The probability
     *
     * @since 1.00
     */
    public float getProbability(final int... states) {
        checkInputStates(false, 0, states);

        return transitionProbabilityArray[toIndex(states)];
    }

    /**
     * Sets a transition probability to a new value.
     * Will put the object into a state where isNormalized() returns false.
     *
     * @param probability the new probability (>= 0).
     * @param states The history of states, from least recent to most recent.
     *               The amount of states given as history should equal the order of the Markov Chain.
     *
     * @return this object
     *
     * @since 1.00
     */
    public RawMarkovChain setProbability(final float probability, final int ... states) {
        // check preconditions //////////////////////
        // error messages ///////////////////////
        final String invalidProbability =
                "Invalid probability value: %s";

        // checks //////////////////////////////

        Preconditions.checkArgument(
                probability >= 0f,
                invalidProbability,
                probability
        );

        checkInputStates(false, 1, states);

        // method body /////////////////////////////
        transitionProbabilityArray[toIndex(states)] = probability;
        isNormalized = false;

        return this;
    }

    /**
     * Normalizes the probability matrix.
     * Will put the object into a state where isNormalized() returns true.
     *
     * @return this object
     *
     * @since 1.00
     */
    public RawMarkovChain normalizeProbabilities() {
        if (!isNormalized) {
            int[] indices = new int[order];

            do {
                normalizeProbabilities(indices);
            } while (increment(indices));

            isNormalized = true;
        }
        return this;
    }

    /**
     * Checks if the object is in a normalized state.
     * @return true if the object is normalized, false otherwise
     *
     * @since 1.00
     */
    public boolean isNormalized() {
        return isNormalized;
    }

    // private /////////////////////////////////////////////////////////

    private int toIndex(final int ... states) {
        int index = 0;

        for (int i = 0            , statePower = TeraMath.pow(nrOfStates, order);
             i < states.length;
             i++              , statePower /= nrOfStates
            ) {
            index += states[i] * statePower;
        }

        return index;
    }

    private int lastIndex(int ... states) {
        return toIndex(states) + nrOfStates - 1;
    }

    private void normalizeProbabilities(final int ... states) {
        // function scope data /////////////////////

        final int startIndex = toIndex(states);
        final int endIndex = lastIndex(states);

        // normalization //////////////////////////

        final float sumOfProbabilitiesPreNorm =
                sumOfProbabilities(startIndex, endIndex);

        if (sumOfProbabilitiesPreNorm > 0f) {
            for (int i = startIndex; i <= endIndex; i++) {
                transitionProbabilityArray[i] /= sumOfProbabilitiesPreNorm;
            }

        } else {
            float probability = 1.0f / nrOfStates;
            for (int i = startIndex; i <= endIndex; i++) {
                transitionProbabilityArray[i] = probability;
            }
        }

        // numerical instability correction ///////

        final float sumOfProbabilitiesPostNorm =
                sumOfProbabilities(startIndex, endIndex);

        if (sumOfProbabilitiesPostNorm < 1.0f) { // >1.0f is fine
            final float error = 1.0f - sumOfProbabilitiesPostNorm;
            int i = endIndex;

            while (transitionProbabilityArray[i] == 0f) {
                i--;
            }

            transitionProbabilityArray[i] += error;
        }

    }

    private float sumOfProbabilities(final int startIndex, final int endIndex) {
        float sumOfProbabilities = 0f;

        for (int i = startIndex; i <= endIndex; i++) {
            sumOfProbabilities += transitionProbabilityArray[i];
        }

        return sumOfProbabilities;
    }

    private boolean increment(int[] indices) {
        for (int i = indices.length - 1; i >= 0; i--) {
            indices[i]++;
            indices[i] %= nrOfStates;

            if (indices[i] != 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validates the states given as argument.
     * Throws an exception if the nr of states does not match the order or
     * when one ore more states are outside of the range [0, nrOfStates).
     * @param asHistory Set to true for calling functions using the states as history,
     *                  Set to false for calling functions using the states as transition index
     * @param argumentOffset The argument nr of the states in the calling function.
     *                       Used to generate a correct error message.
     * @param states The states that will be validated.
     */
    private void checkInputStates(boolean asHistory, final int argumentOffset, final int ... states) {
        // error message ////////////////////////////
        final String nrOfStatesMismatchHistoryFormat =
                "Received %s states. Nr of states given as should match the order (=%s).";

        final String nrOfStatesMismatchIndexFormat =
                "Received %s states. Nr of states given as should match the order + 1 (=%s).";

        final String invalidStateAsArgumentFormat =
                "Argument %s = %s is not in the range [0, %s), which is not a valid state.";

        // check preconditions //////////////////////
        if (asHistory) {
            Preconditions.checkArgument(
                    states.length == order,
                    nrOfStatesMismatchHistoryFormat,
                    states.length, order
            );
        } else {
            Preconditions.checkArgument(
                    states.length == order + 1,
                    nrOfStatesMismatchIndexFormat,
                    states.length, order + 1
            );
        }


        int invalidStateNr = firstInvalidState(states);
        int invalidStateValue = (invalidStateNr == NO_INVALID_STATES) ? 0 : states[invalidStateNr];
        Preconditions.checkArgument(
                invalidStateNr == NO_INVALID_STATES,
                invalidStateAsArgumentFormat,
                invalidStateNr + argumentOffset, invalidStateValue, nrOfStates
        );
    }

    /**
     * Checks if all states are in the range [0, nrOfStates).
     * @param states The states that will be checked.
     * @return Returns NO_INVALID_STATES if all states are valid,
     *         Otherwise, returns the number of first invalid argument.
     */
    private int firstInvalidState(final int... states) {
        for (int i = 0; i < states.length; i++) {
            if (0 > states[i] || states[i] >= nrOfStates) { //not in range [0, nrOfStates)
                return i;
            }
        }

        return NO_INVALID_STATES;
    }
}
