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
import org.terasology.math.TeraMath;

import java.util.Arrays;

/**
 * An data structure that explicitly stores all transition probability values into a dense matrix.
 *
 * The storage requirement is O( k^(i+1) ), where k is the number of states and i is the order.
 * Since the matrix grows exponentially with the order, it is only suitable for Markov chains with a low order.
 * @since 1.50
 */
public class ExplicitTransitionMatrix extends TransitionMatrix {
    private boolean isNormalized;
    private final float[] transitionProbabilityArray;

    // public //////////////////////////////////////////////////////////

    /**
     * Constructs a transition matrix for a second order Markov chain.
     *
     * The constructed object will be in a state where isNormalized() returns false.
     *
     * @param transitionMatrix A 3d matrix of transition probabilities.
     *                      Every element probabilities[x][y][z] determines the probability of transitioning
     *                      to state z, given previous state y and second previous state x.
     *                      The matrix must be cubical.
     *
     * @since 1.50
     */
    public ExplicitTransitionMatrix(final float[][][] transitionMatrix) {
        this(2, transitionMatrix[0][0].length, flatten(transitionMatrix));
    }

    /**
     * Constructs a transition matrix for a (first order) Markov chain.
     *
     * The constructed object will be in a state where isNormalized() returns false.
     *
     * @param transitionMatrix A 2d matrix of transition probabilities.
     *                      Every element probabilities[x][y] determines the probability of transitioning
     *                      from state x to state y.
     *                      The matrix must be square.
     *
     * @since 1.50
     */
    public ExplicitTransitionMatrix(final float[][] transitionMatrix) {
        this(1, transitionMatrix[0].length, flatten(transitionMatrix));
    }

    /**
     * Constructs a transition matrix for a Markov chain of any order and any nr of states.
     *
     * The constructed object will be in a state where isNormalized() returns false.
     *
     * <b>Note:</b> Avoid calling this constructor directly. Use the more user friendly, 2D and 3D array versions
     * whenever possible.
     *
     * @param order The order (>= 1) of the Markov chain,
     *      i.e. how many (previous) states are considered to compute the next.
     * @param nrOfStates The nr of states (>=1).
     * @param probabilities The transition probabilities of length pow(nrOfStates, order + 1).
     *      The provided array should be a flattened n-dimensional array of probabilities, with
     *      n being the order of the Markov chain.
     *
     * @since 1.50
     */
    public ExplicitTransitionMatrix(final int order, final int nrOfStates, final float[] probabilities) {
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

        final int requiredNrOfProbabilities = TeraMath.pow(getNrOfStates(), getOrder() + 1);

        Preconditions.checkArgument(
                probabilities.length == requiredNrOfProbabilities,
                probabilitiesArgumentExceptionFormat,
                probabilities.length, order, nrOfStates, requiredNrOfProbabilities
        );

        // initialize private data ////////////////////////////////////////////////

        transitionProbabilityArray = Arrays.copyOf(probabilities, requiredNrOfProbabilities);
        isNormalized = false;
    }

    public ExplicitTransitionMatrix(final int order, final int nrOfStates) {
        super(order, nrOfStates);

        transitionProbabilityArray = createTransitionArray(order, nrOfStates);
        isNormalized = false;
    }

    public float get(final int... states) {
        checkInputStates(false, 0, states);

        return transitionProbabilityArray[toIndex(states)];
    }

    /**
     * Sets the transition probabilities for all target states for a specific history.
     * Will put the object into a state where isNormalized() returns false.
     *
     * @param probabilities the new probabilities (all >= 0).
     * @param states The history (X_0 .. X_n) states in the order X_0 to X_n.
     *               The amount of states given as history should equal the order of the Markov chain - 1.
     *
     * @return this object
     *
     * @since 1.50
     */
    public ExplicitTransitionMatrix setRow(final float[] probabilities, final int ... states) {
        final String invalidProbabilityArrayLength =
                "Probability array length should match the number of states (%s), but was %s";

        final String invalidProbability =
                "Invalid probability value at index %s: %s";

        // checks //////////////////////////////

        Preconditions.checkArgument(
                probabilities.length == getNrOfStates(),
                invalidProbabilityArrayLength,
                getNrOfStates(), probabilities.length
        );

        checkInputStates(true, 1,  states);

        for(int i = 0; i < probabilities.length; i++) {
            Preconditions.checkArgument(
                    probabilities[i] >= 0f,
                    invalidProbability,
                    i, probabilities[i]
            );
        }

        // method body /////////////////////////////
        int indexOffset = toIndex(states);

        for(int i = 0; i < probabilities.length; i++) {
            transitionProbabilityArray[indexOffset + i] = probabilities[i];
        }
        isNormalized = false;

        return this;
    }

    /**
     * Sets a transition probability to a new value.
     * Will put the object into a state where isNormalized() returns false.
     *
     * @param probability the new probability (>= 0).
     * @param states The history (X_0 .. X_n-1) states and target state X_n in the order X_0 to X_n.
     *               The amount of states given as history should equal the order of the Markov chain.
     *
     * @return this object
     *
     * @since 1.50
     */
    public ExplicitTransitionMatrix set(final float probability, final int ... states) {
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

    public ExplicitTransitionMatrix normalize() {
        if (!isNormalized) {
            int[] indices = new int[getOrder()];

            do {
                normalizeRow(indices);
            } while (increment(indices));

            isNormalized = true;
        }
        return this;
    }

    public boolean isNormalized() {
        return isNormalized;
    }

    // private /////////////////////////////////////////////////////////

    private int toIndex(final int ... states) {
        int index = 0;

        for (int i = 0           , statePower = TeraMath.pow(getNrOfStates(), getOrder());
                i < states.length;
                i++              , statePower /= getNrOfStates()
            ) {
            index += states[i] * statePower;
        }

        return index;
    }

    private int lastIndex(int ... states) {
        return toIndex(states) + getNrOfStates() - 1;
    }

    private void normalizeRow(final int ... states) {
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
            float probability = 1.0f / getNrOfStates();
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
            indices[i] %= getNrOfStates();

            if (indices[i] != 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Convenience method to create a transition array of the right size for
     * a given order and nr of states.
     * @param order The order of the Markov chain
     * @param nrOfStates The nr of states in the Markov chain
     * @return The transition array, filled with zeros.
     *
     * @since 1.50
     */
    public static float[] createTransitionArray(final int order, final int nrOfStates) {
        return new float[TeraMath.pow(nrOfStates, order + 1)];
    }

    /**
     * Flattens a 3D transition matrix into a (1D) transition array)
     * @param probabilities The transition matrix.
     * @return The equivalent transition array.
     *
     * @since 1.50
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
     * @since 1.50
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
