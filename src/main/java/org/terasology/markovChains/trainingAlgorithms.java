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

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of algorithms that can create and train a Markov Chain from training data.
 *
 * @version 1.00
 * @author Linus van Elswijk
 */
public final class TrainingAlgorithms {

    private TrainingAlgorithms() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * Creates a {@link MarkovChain}, trained on a list of sample sequences.
     *
     * The samples will be used to calculate the probability of generating a state, given
     * a certain history of previously generated states.
     * The resulting Markov chain will be able to produce random sequences that are similar to
     * the samples.
     *
     * @param order The order of the trained markov chain.
     * @param states The list of possible states.
     * @param sampleSequences The examples used for training.
     * @param endState Ending a sequence is will be represented as going to this state. Use null if you
     *                 don't want to use a termination state.
     * @param <S> The type of the states.
     * @return The trained {@link MarkovChain}.
     *
     * @since 1.00
     */
    public static <S> MarkovChain<S> forwardAlgorithm(final int order,
                                                      final List<S> states,
                                                      S[][] sampleSequences,
                                                      S endState
    ) {
        // preparation
        final int nrOfStates = states.size();
        final float[] transitionArray = RawMarkovChain.createTransitionArray(order, nrOfStates);

        RawMarkovChain rawMarkovChain =
                new RawMarkovChain(order, nrOfStates, transitionArray);

        // forward algorithm body

        for (S[] sequence: sampleSequences) {
            // sequence preparation
            Deque<Integer> history = new LinkedList<>();

            while (history.size() < (order)) {
                history.add(0);
            }

            // process sequence

            for (S state : sequence) {
                history.addLast(states.indexOf(state));

                adjustProbability(rawMarkovChain, history, +1f);

                history.removeFirst();
            }

            if (endState != null) {
                history.addLast(states.indexOf(endState));

                adjustProbability(rawMarkovChain, history, +1f);

                history.removeFirst();
            }
        }

        rawMarkovChain.normalizeProbabilities();

        return new MarkovChain<>(order, states, rawMarkovChain, new FastRandom());
    }

    private static void adjustProbability(RawMarkovChain markovChain, Deque<Integer> states, float delta) {
        int[] stateArray = toIntArray(states);

        markovChain.setProbability(markovChain.getProbability(stateArray) + delta, stateArray);
    }

    private static int[] toIntArray(Deque<Integer> states) {
        Deque<Integer> intObjects = new LinkedList<>(states);

        int[] intArray = new int[intObjects.size()];

        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = intObjects.removeFirst();
        }

        return intArray;
    }
}
