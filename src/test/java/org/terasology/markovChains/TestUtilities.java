// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.markovChains;

import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.markovChains.dataStructures.ExplicitTransitionMatrix;
import org.terasology.math.TeraMath;

/**
 * General utility stuff used to test multiple classes.
 */
public final class TestUtilities {

    public static final Random RANDOM_NUMBER_GENERATOR = new FastRandom(7357);

    private TestUtilities() {
    }

    public static float[][] randomTransitionArray(final int order, final int states) {
        float[][] probabilities = new float[2][TeraMath.pow(states, order + 1)];

        for (int i = 0; i < probabilities[0].length; i += states) {
            float[] iProbabilities = new float[states];
            float sumOfProbabilities = 0f;

            for (int state = 0; state < states; state++) {
                iProbabilities[state] = RANDOM_NUMBER_GENERATOR.nextFloat();
                sumOfProbabilities += iProbabilities[state];
            }

            for (int state = 0; state < states; state++) {
                probabilities[0][i + state] = iProbabilities[state];
                probabilities[1][i + state] = iProbabilities[state] / sumOfProbabilities;
            }
        }

        return probabilities;
    }

    public static float[] skipNTransitionArray(final int n, final int order, final int states) {
        float[] probabilities = new float[TeraMath.pow(states, order + 1)];

        int cycleLength = states * states;

        for (int i = 0; i < probabilities.length; i++) {
            final int previousState = i % cycleLength / states;
            final int currentState = i % states;

            if ((previousState + n) % states == currentState) {
                probabilities[i] = 1f;
            } else {
                probabilities[i] = 0f;
            }
        }

        return probabilities;
    }

    public static float[] doNotSkipNTransitionArray(final int n, final int order, final int states) {
        float[] probabilities = skipNTransitionArray(n, order, states);
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] = 1.0f - probabilities[i];
        }

        return probabilities;
    }

    public static ExplicitTransitionMatrix randomTransitionMatrix(final int order, final int states) {
        return new ExplicitTransitionMatrix(order, states, randomTransitionArray(order, states)[0]);
    }
}
