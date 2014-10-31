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
import org.terasology.math.TeraMath;

import java.util.Iterator;
import java.util.List;

/**
 * N-order Markov Chain implementation.
 * This is the raw version with a low level interface.
 *
 * @since 31-10-2014
 * @author Linus van Elswijk
 */
public class RawMarkovChain extends MarkovChainBase {

    // public //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a second order Markov Chain.
     *
     * @param transitionMatrix A 3d matrix of transition probabilities.
     *                      Every element probabilities[x][y][z] determines the probability of transitioning
     *                      to state z, given previous state y and second previous state x.
     *                      The matrix must be cubical.
     */
    public RawMarkovChain(final float[][][] transitionMatrix) {
        this(2, transitionMatrix[0][0].length, flatten(transitionMatrix));
    }

    /**
     * Constructs a (first order) Markov Chain.
     *
     * @param probabilities A 2d matrix of transition probabilities.
     *                      Every element probabilities[x][y] determines the probability of transitioning
     *                      from state x to state y.
     *                      The matrix must be square.
     */
    public RawMarkovChain(final float[][] probabilities) {
        this(1, probabilities[0].length, flatten(probabilities));
    }

    /**
     * Constructs a Markov Chain of any order and any nr of states.
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
     */
    public RawMarkovChain(final int order, final int nrOfStates, final float[] probabilities) {
        super(order, nrOfStates);

        // argument exception message formats //////////////////////////////////////

        final String PROBABILITIES_ARGUMENT_EXCEPTION_MESSAGE = "probabilities.length=%s, with order=%s and nrOfStates=%s the expected length is %s";

        // check argument preconditions  ///////////////////////////////////////////

        final int REQUIRED_NR_OF_PROBABILITIES = TeraMath.pow(NR_OF_STATES, ORDER + 1);

        Preconditions.checkArgument(
                probabilities.length == REQUIRED_NR_OF_PROBABILITIES,
                PROBABILITIES_ARGUMENT_EXCEPTION_MESSAGE,
                probabilities.length, order, nrOfStates, REQUIRED_NR_OF_PROBABILITIES
        );

        // initialize private data ////////////////////////////////////////////////

        transitionProbabilityArray = new float[REQUIRED_NR_OF_PROBABILITIES];

        for(int i = 0; i < probabilities.length; i++) {
            this.transitionProbabilityArray[i] = probabilities[i];
        }
        normalizeProbabilities();
    }

    /**
     * Returns the next state, given a random number and state history.
     * @param randomNumber a random number in the range [0, 1).
     * @param states The history of states, from least recent to most recent.
     *               The amount of states given as history should equal the order of the Markov Chain.
     * @return The next state
     */
    public int getNext(float randomNumber, List<Integer> states) {
        int[] statesArray = new int[states.size()];

        {
            int i = 0;
            for(Iterator<Integer> it = states.iterator(); it.hasNext(); i++) {
                statesArray[i] = it.next();
            }
        }

        return getNext(randomNumber, statesArray);
    }

    /**
     * Returns the next state, given a random number and state history.
     * @param randomNumber a random number in the range [0, 1).
     * @param states The history of states, from least recent to most recent.
     *               The amount of states given as history should equal the order of the Markov Chain.
     * @return The next state
     */
    public int getNext(float randomNumber, int ... states ) {
        // check preconditions //////////////////////
        Preconditions.checkArgument(states.length == ORDER, NR_OF_STATES_MISMATCH_MESSAGE, states.length, ORDER);

        // method body /////////////////////////////
        final int START_INDEX = toIndex(states);
        final int END_INDEX = lastIndex(states);

        for(int i = START_INDEX; i < END_INDEX; i++) {
            randomNumber -= transitionProbabilityArray[i];
            if(randomNumber < 0)
                return i % NR_OF_STATES;
        }

        return END_INDEX % NR_OF_STATES;
    }

    /**
     * Returns the probability of transitioning to a state X_n, given history X_0 .. X_n-1
     * @param states The history (X_0 .. X_n-1) states and target state X_n in the order X_0 to X_n.
     *               The amount of states given as history should equal the order of the Markov Chain.
     * @return The probability
     */
    public float getProbability(final int... states) {
        Preconditions.checkArgument(states.length == ORDER, NR_OF_STATES_MISMATCH_MESSAGE, states.length, ORDER);

        return transitionProbabilityArray[toIndex(states)];
    }

    // private /////////////////////////////////////////////////////////////////////////////////////////////////////////

    private int toIndex(final int ... states) {
        int index = 0;

        for(int i = 0            , statePower = TeraMath.pow(NR_OF_STATES, ORDER);
                i < states.length;
                i++              , statePower /= NR_OF_STATES
                ) {
            index += states[i] * statePower;
        }

        return index;
    }

    private int lastIndex(int ... states) {
        return toIndex(states) + NR_OF_STATES - 1;
    }

    private void normalizeProbabilities() {
        int[] indices = new int[ORDER];

        do {
            normalizeProbabilities(indices);
        } while( increment(indices) );
    }

    private void normalizeProbabilities(final int ... states) {
        final int START_INDEX = toIndex(states);
        final int END_INDEX = lastIndex(states);

        float sumOfProbabilities = 0;

        for(int i = START_INDEX; i <= END_INDEX; i++) {
            sumOfProbabilities += transitionProbabilityArray[i];
        }

        for(int i = START_INDEX; i <= END_INDEX; i++) {
            transitionProbabilityArray[i] /= sumOfProbabilities;
        }
    }

    private boolean increment(int[] indices) {
        for(int i = indices.length - 1; i >= 0; i--) {
            indices[i]++;
            indices[i] %= NR_OF_STATES;

            if(indices[i] != 0)
                return true;
        }

        return false;
    }


    private final float[] transitionProbabilityArray;


    private static float[] flatten(final float[][][] probabilities) {
        final int WIDTH  = probabilities.length;
        final int HEIGHT = probabilities[0].length;
        final int DEPTH = probabilities[0][0].length;

        float[] flattened = new float[WIDTH * HEIGHT * DEPTH];

        for(int i=0 , x=0; x < WIDTH; x++) {
            for(int y=0; y < HEIGHT; y++) {
                for(int z=0; z < DEPTH; z++, i++) {
                    flattened[i] = probabilities[x][y][z];
                }
            }
        }

        return flattened;
    };

    private static float[] flatten(final float[][] probabilities) {
        final int WIDTH  = probabilities.length;
        final int HEIGHT = probabilities[0].length;

        float[] flattened = new float[WIDTH * HEIGHT];

        for(int i=0 , x=0; x < WIDTH; x++) {
            for(int y=0; y < HEIGHT; y++, i++) {
                flattened[i] = probabilities[x][y];
            }
        }

        return flattened;
    };


    private final String NR_OF_STATES_MISMATCH_MESSAGE =
            "Received %s states. Nr of states given as should match the order (=%s).";
}
