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
package org.terasology.markovChains;

import org.terasology.markovChains.dataStructures.ExplicitTransitionMatrix;
import org.terasology.math.TeraMath;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/**
 * General utility stuff used to test multiple classes.
 */
public final class TestUtilities {

    public static final Random RANDOM_NUMBER_GENERATOR = new FastRandom(7357);

    private TestUtilities() { }

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
