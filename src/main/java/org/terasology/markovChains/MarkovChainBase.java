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

/**
 * Abstract base class for Markov Chain implementations.
 * Defines a minimal common interface.
 * @author Linus van Elswijk
 * @version 1.00
 */
public abstract class MarkovChainBase {

    // public //////////////////////////////////////////////////////////

    /**
     * The order of the Markov Chain
     *
     * @since 1.00
     */
    public final int order;

    /**
     * The number of states in the Markov Chain.
     *
     * @since 1.00
     */
    public final int nrOfStates;

    /**
     * Constructs a Markov Chain of any order and any nr of states.
     *
     * @param order The order (>= 1) of the Markov Chain,
     *      i.e. how many (previous) states are considered to compute the next.
     * @param nrOfStates The nr of states (>=1).
     *
     * @since 1.00
     */
    protected MarkovChainBase(final int order, final int nrOfStates) {
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
     * Convenience method to create a transition array of the right size for
     * a given order and nr of states.
     * @param order The order of the Markov chain
     * @param nrOfStates The nr of states in the Markov chain
     * @return The transition array, filled with zeros.
     *
     * @since 1.00
     */
    public static float[] createTransitionArray(final int order, final int nrOfStates) {
        return new float[TeraMath.pow(nrOfStates, order + 1)];
    }

    /**
     * Flattens a 3D transition matrix into a (1D) transition array)
     * @param probabilities The transition matrix.
     * @return The equivalent transition array.
     *
     * @since 1.00
     */
    protected static float[] flatten(final float[][][] probabilities) {
        final int width  = probabilities.length;
        final int height = probabilities[0].length;
        final int depth = probabilities[0][0].length;

        float[] flattened = new float[width * height * depth];

        int i = 0;
        for (float[][] slice : probabilities) {
            for (float[] row : slice) {
                for (float probability : row) {
                    flattened[i] = probability;
                    i++;
                }
            }
        }

        return flattened;
    }

    /**
     * Flattens a 2D transition matrix into a (1D) transition array)
     * @param probabilities The transition matrix.
     * @return The equivalent transition array.
     * @since 1.00
     */
    protected static float[] flatten(final float[][] probabilities) {
        final int width  = probabilities.length;
        final int height = probabilities[0].length;

        float[] flattened = new float[width * height];

        int i = 0;
        for (float[] row : probabilities) {
            for (float probability : row) {
                flattened[i] = probability;
                i++;
            }
        }

        return flattened;
    }

}
