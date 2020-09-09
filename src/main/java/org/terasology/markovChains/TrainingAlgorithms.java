// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.markovChains;

import org.terasology.markovChains.dataStructures.ExplicitTransitionMatrix;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of algorithms that can create and train a Markov Chain from training data.
 *
 * @version 1.50
 * @since 1.00
 */
public final class TrainingAlgorithms {

    private TrainingAlgorithms() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * Creates a {@link MarkovChain}, trained on a list of sample sequences.
     * <p>
     * The samples will be used to calculate the probability of generating a state, given a certain history of
     * previously generated states. The resulting Markov chain will be able to produce random sequences that are similar
     * to the samples.
     *
     * @param order The order of the trained markov chain.
     * @param states The list of possible states.
     * @param sampleSequences The examples used for training.
     * @param endState Ending a sequence is will be represented as going to this state. Use null if you don't
     *         want to use a termination state.
     * @param <S> The type of the states.
     * @return The trained {@link MarkovChain}.
     * @since 1.00
     */
    public static <S> MarkovChain<S> forwardAlgorithm(final int order,
                                                      final List<S> states,
                                                      Collection<S[]> sampleSequences,
                                                      S endState
    ) {
        // preparation
        final int nrOfStates = states.size();
        final ExplicitTransitionMatrix transitionMatrix = new ExplicitTransitionMatrix(order, nrOfStates);

        //RawMarkovChain rawMarkovChain = new RawMarkovChain(order, nrOfStates, transitionArray);

        // forward algorithm body

        for (S[] sequence : sampleSequences) {
            // sequence preparation
            Deque<Integer> history = new LinkedList<>();

            while (history.size() < (order)) {
                history.add(0);
            }

            // process sequence

            for (S state : sequence) {
                history.addLast(states.indexOf(state));

                adjustProbability(transitionMatrix, history, +1f);

                history.removeFirst();
            }

            if (endState != null) {
                history.addLast(states.indexOf(endState));

                adjustProbability(transitionMatrix, history, +1f);

                history.removeFirst();
            }
        }

        transitionMatrix.normalize();

        return new MarkovChain<>(states, transitionMatrix);
    }

    private static void adjustProbability(ExplicitTransitionMatrix transitionMatrix, Deque<Integer> states,
                                          float delta) {
        int[] stateArray = toIntArray(states);

        transitionMatrix.set(transitionMatrix.get(stateArray) + delta, stateArray);
    }

    private static int[] toIntArray(Deque<Integer> states) {
        int index = 0;
        int[] intArray = new int[states.size()];

        for (Integer val : states) {
            intArray[index++] = val;
        }

        return intArray;
    }
}
