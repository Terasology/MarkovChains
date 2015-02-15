/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.markovChains.dataStructures;

import com.google.common.base.Preconditions;

import java.util.Arrays;

/**
 * An abstract base class for objects that describe the transition matrix of a Markov chain.
 *
 * @since 1.50
 * @version 1.50
 * @author Linus van Elswijk
 */
public abstract class TransitionMatrix {

    /**
     * Special value that is returned by the firstInvalidState method,
     * when all states are valid.
     */
    public static final int NO_INVALID_STATES = -1;

    private final int order;
    private final int nrOfStates;

    /**
     * Constructs a Markov Chain of any order and any nr of states.
     *
     * @param order The order (>= 1) of the Markov Chain,
     *      i.e. how many (previous) states are considered to compute the next.
     * @param nrOfStates The nr of states (>=1).
     *
     * @since 1.50
     */
    protected TransitionMatrix(final int order, final int nrOfStates) {
        final String stateArgumentExceptionFormat = "nrOfStates=%s, should be >= 1";
        final String orderArgumentExceptionFormat = "order=%s, should be >= 1";

        Preconditions.checkArgument(
                nrOfStates > 0,
                stateArgumentExceptionFormat,
                nrOfStates
        );

        Preconditions.checkArgument(
                order > 0,
                orderArgumentExceptionFormat,
                order
        );

        this.order = order;
        this.nrOfStates = nrOfStates;
    }

    /**
     * Returns the order of the Markov chain for which the transition matrix is suitable.
     * @return The order
     */
    public final int getOrder() {
        return order;
    }

    /**
     * Returns the number of states of the Markov chain for which the transition matrix is suitable.
     * @return The number of states
     */
    public final int getNrOfStates() {
        return nrOfStates;
    }

    /**
     * Returns the probability of transitioning to a state X_n, given history X_0 .. X_n-1
     * @param states The history (X_0 .. X_n-1) states and target state X_n in the order X_0 to X_n.
     *               The amount of states given as history should equal the order of the Markov Chain.
     * @return The probability
     *
     * @since 1.50
     */
    public abstract float get(final int... states);

    /**
     * Returns the probabilities of transitioning to the next states, given history X_0 .. X_n-1
     * @param states The history (X_0 .. X_n-1) states.
     *               The amount of states given as history should equal the order of the Markov Chain - 1.
     * @return An array with at position i the probability of transitioning towards state X_i.
     *
     * @since 1.50
     */
    public final float[] getRow(final int... states) {
        checkInputStates(true, 0, states);

        int[] transitionIndex = Arrays.copyOf(states, states.length + 1);
        float[] probabilities = new float[getNrOfStates()];

        for (int i = 0; i < probabilities.length; i++) {
            transitionIndex[states.length] = i; //sets the last element
            probabilities[i] = get(transitionIndex);
        }

        return probabilities;
    }

    /**
     * Checks if the object is in a normalized state.
     *
     * A transitionMatrix is considered to be normalized if and only if all rows of the transitionMatrix add up to 1.
     *
     * @return true if the object is normalized, false otherwise
     *
     * @since 1.50
     */
    public boolean isNormalized() {
        return false;
    }

    // argument checking /////////////////////////////////////////////////

    /**
     * Validates the states given as argument.
     * Throws an exception if the nr of states does not match the order or
     * when one ore more states are outside of the range [0, nrOfStates).
     * @param asHistory Set to true for calling functions using the states as history,
     *                  Set to false for calling functions using the states as transition index
     * @param argumentOffset The argument nr of the states in the calling function.
     *                       Used to generate a correct error message.
     * @param states The states that will be validated.
     *
     * @since 1.50
     */
    public void checkInputStates(boolean asHistory, final int argumentOffset, final int ... states) {
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
     *
     * @since 1.50
     */
    public int firstInvalidState(final int... states) {
        for (int i = 0; i < states.length; i++) {
            if (0 > states[i] || states[i] >= nrOfStates) { //not in range [0, nrOfStates)
                return i;
            }
        }

        return NO_INVALID_STATES;
    }

}
