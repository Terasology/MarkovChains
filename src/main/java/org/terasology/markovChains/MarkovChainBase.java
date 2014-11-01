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

/**
 * Abstract base class for Markov Chain implementations.
 * Defines a minimal common interface.
 */
public abstract class MarkovChainBase {

    // public //////////////////////////////////////////////////////////

    /**
     * The order of the Markov Chain
     */
    public final int ORDER;

    /**
     * The number of states in the Markov Chain.
     */
    public final int NR_OF_STATES;

    // protected ///////////////////////////////////////////////////////

    /**
     * Constructs a Markov Chain of any order and any nr of states.
     *
     * @param order The order (>= 1) of the Markov Chain,
     *      i.e. how many (previous) states are considered to compute the next.
     * @param nrOfStates The nr of states (>=1).
     */
    protected MarkovChainBase(final int order, final int nrOfStates) {
        final String STATE_ARGUMENT_EXCEPTION_MESSAGE = "nrOfStates=%s, should be >= 1",
                ORDER_ARGUMENT_EXCEPTION_MESSAGE = "order=%s, should be >= 1";

        Preconditions.checkArgument(
                nrOfStates > 0,
                STATE_ARGUMENT_EXCEPTION_MESSAGE,
                nrOfStates
        );

        Preconditions.checkArgument(
                order > 0,
                ORDER_ARGUMENT_EXCEPTION_MESSAGE,
                order
        );

        this.ORDER = order;
        this.NR_OF_STATES = nrOfStates;
    }

    protected static float[] flatten(final float[][][] probabilities) {
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
    }

    protected static float[] flatten(final float[][] probabilities) {
        final int WIDTH  = probabilities.length;
        final int HEIGHT = probabilities[0].length;

        float[] flattened = new float[WIDTH * HEIGHT];

        for(int i=0 , x=0; x < WIDTH; x++) {
            for(int y=0; y < HEIGHT; y++, i++) {
                flattened[i] = probabilities[x][y];
            }
        }

        return flattened;
    }
}
