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

import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.util.LinkedList;
import java.util.List;

/**
 * A collection of algorithms that can create and train a Markov Chain from training data.
 *
 * Created by Linus on 2-11-2014.
 */
public final class TrainingAlgorithms {

    public static <S> MarkovChain<S> forwardAlgorithm(final int order,
                                                      final List<S> states,
                                                      S[][] sampleSequences,
                                                      S endState
    ) {
        // preparation
        final int NR_OF_STATES = states.size();
        final float[] transitionArray = RawMarkovChain.createTransitionArray(order, NR_OF_STATES);

        RawMarkovChain rawMarkovChain =
                new RawMarkovChain(order, NR_OF_STATES, transitionArray);

        Random random = new FastRandom();

        // forward algorithm body

        for(S[] sequence: sampleSequences) {
            // sequence preparation
            LinkedList<Integer> history = new LinkedList<>();

            while(history.size() < (order)) {
                history.add(0);
            }

            // process sequence

            for(S state : sequence) {
                history.addLast(states.indexOf(state));

                adjustProbability(rawMarkovChain, history, +1f);

                history.removeFirst();
            }

            if(endState != null) {
                history.addLast(states.indexOf(endState));

                adjustProbability(rawMarkovChain, history, +1f);

                history.removeFirst();
            }
        }

        rawMarkovChain.normalizeProbabilities();

        return new MarkovChain<S>(order, states, rawMarkovChain, new FastRandom() );
    }

    private static void adjustProbability(RawMarkovChain markovChain, List<Integer> states, float delta) {
        int[] stateArray = toIntArray(states);

        markovChain.setProbability(markovChain.getProbability(stateArray) + delta, stateArray);
    }

    private static int[] toIntArray(List<Integer> states) {
        LinkedList<Integer> intObjects = new LinkedList<>(states);

        int[] intArray = new int[intObjects.size()];

        for(int i = 0; i < intArray.length; i++) {
            intArray[i] = intObjects.removeFirst();
        }

        return intArray;
    }

    private TrainingAlgorithms() {
        throw new RuntimeException("TrainingAlgorithm should not be instantiated");
    }
}