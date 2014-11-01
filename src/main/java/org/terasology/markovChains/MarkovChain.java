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
import com.google.common.collect.ImmutableList;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.util.*;

/**
 * N-order Markov Chain implementation.
 * This is the user friendly version.
 *
 * If more control over the internal state is necessary, use {@link RawMarkovChain} instead.
 *
 * @tparam S The type of the states.
 *
 * @since 31-10-2014
 * @author Linus van Elswijk
 */
public class MarkovChain<S> extends MarkovChainBase {

    // 2nd order Markov Chain constructors //////////////////////////////

    /**
     * Constructs a second order Markov Chain.
     *
     * @param states The states in the chain.
     * @param transitionMatrix A 3d matrix of transition probabilities.
     *                      Every element probabilities[x][y][z] determines the probability of transitioning
     *                      to state z, given current state y and previous state x.
     *                      The matrix must be cubical.
     */
    public MarkovChain(List<S> states, float[][][] transitionMatrix) {
        this(2, states, flatten(transitionMatrix));
    }

    /**
     * Constructs a second order Markov Chain.
     *
     * @param states The states in the chain.
     * @param transitionMatrix A 3d matrix of transition probabilities.
     *                      Every element probabilities[x][y][z] determines the probability of transitioning
     *                      to state z, given current state y and previous state x.
     *                      The matrix must be cubical.
     * @param seed The seed for the random number generator used for determining next states.
     */
    public MarkovChain(List<S> states, float[][][] transitionMatrix, long seed) {
        this(2, states, flatten(transitionMatrix), seed);
    }

    /**
     * Constructs a second order Markov Chain.
     *
     * @param states The states in the chain.
     * @param transitionMatrix A 3d matrix of transition probabilities.
     *                      Every element probabilities[x][y][z] determines the probability of transitioning
     *                      to state z, given current state y and previous state x.
     *                      The matrix must be cubical.
     * @param random The random number generator used for determining next states.
     */
    public MarkovChain(List<S> states, float[][][] transitionMatrix, Random random) {
        this(2, states, flatten(transitionMatrix), random);
    }

    // 1st order Markov Chain constructors //////////////////////////////

    /**
     * Constructs a first order Markov Chain.
     *
     * @param states The states in the chain.
     * @param transitionMatrix A 2d matrix of transition probabilities.
     *                      Every element probabilities[x][y] determines the probability of transitioning
     *                      to state y, given current state x.
     *                      The matrix must be square.
     */
    public MarkovChain(List<S> states, float[][] transitionMatrix) {
        this(1, states, flatten(transitionMatrix));
    }

    /**
     * Constructs a first order Markov Chain.
     *
     * @param states The states in the chain.
     * @param transitionMatrix A 2d matrix of transition probabilities.
     *                      Every element probabilities[x][y] determines the probability of transitioning
     *                      to state y, given current state x.
     *                      The matrix must be square.
     * @param seed The seed for the random number generator used for determining next states.
     */
    public MarkovChain(List<S> states, float[][] transitionMatrix, long seed) {
        this(1, states, flatten(transitionMatrix), seed);
    }

    /**
     * Constructs a first order Markov Chain.
     *
     * @param states The states in the chain.
     * @param transitionMatrix A 2d matrix of transition probabilities.
     *                      Every element probabilities[x][y] determines the probability of transitioning
     *                      to state y, given current state x.
     *                      The matrix must be square.
     * @param random The random number generator used for determining next states.
     */
    public MarkovChain(List<S> states, float[][] transitionMatrix, Random random) {
        this(1, states, flatten(transitionMatrix), random);
    }

    // n-th order Markov Chain constructors //////////////////////////////

    /**
     * Constructs a Markov Chain of any order and any set of states.
     *
     * <b>Note:</b> Avoid calling this constructor directly. Use the more user friendly, 2D and 3D array versions
     * whenever possible.
     *
     * @param order The order (>= 1) of the Markov Chain,
     *      i.e. how many (previous) states are considered to compute the next.
     * @param states The states in the chain.
     * @param transitionProbabilities The transition probabilities of length pow(nrOfStates, order + 1).
     *      The provided array should be a flattened n-dimensional array of probabilities, with
     *      n being the order of the markov chain.
     */
    public MarkovChain(int order, List<S> states, float[] transitionProbabilities) {
        this(order, states, transitionProbabilities, new FastRandom());
    }

    /**
     * Constructs a Markov Chain of any order and any set of states.
     *
     * <b>Note:</b> Avoid calling this constructor directly. Use the more user friendly, 2D and 3D array versions
     * whenever possible.
     *
     * @param order The order (>= 1) of the Markov Chain,
     *      i.e. how many (previous) states are considered to compute the next.
     * @param states The states in the chain.
     * @param transitionProbabilities The transition probabilities of length pow(nrOfStates, order + 1).
     *      The provided array should be a flattened n-dimensional array of probabilities, with
     *      n being the order of the markov chain.
     * @param seed The seed for the random number generator used for determining next states.
     */
    public MarkovChain(int order, List<S> states, float[] transitionProbabilities, long seed) {
        this(order, states, transitionProbabilities, new FastRandom(seed));
    }

    /**
     * Constructs a Markov Chain of any order and any set of states.
     *
     * <b>Note:</b> Avoid calling this constructor directly. Use the more user friendly, 2D and 3D array versions
     * whenever possible.
     *
     * @param order The order (>= 1) of the Markov Chain,
     *      i.e. how many (previous) states are considered to compute the next.
     * @param states The states in the chain.
     * @param transitionProbabilities The transition probabilities of length pow(nrOfStates, order + 1).
     *      The provided array should be a flattened n-dimensional array of probabilities, with
     *      n being the order of the markov chain.
     * @param random The random number generator used for determining next states.
     */
    public MarkovChain(int order, List<S> states, float[] transitionProbabilities, Random random) {
        super(order, states.size());

        Preconditions.checkArgument(
                allUnique(states),
                ALL_STATES_UNIQUE_MESSAGE
        );

        this.states = ImmutableList.copyOf(states);
        this.rawMarkovChain = new RawMarkovChain(ORDER, NR_OF_STATES, transitionProbabilities);
        this.rawMarkovChain.normalizeProbabilities();
        this.random = random;
        this.history = new LinkedList<S>();
        this.rawHistory = new LinkedList<Integer>();

        while(history.size() <= ORDER) {
            history.push(states.get(0));
            rawHistory.push(0);
        }
    }

    // public interface //////////////////////////////////////////////////

    /**
     * Moves the chain to the next state.
     * @return The next state.
     */
    public S next() {
        history.removeLast();
        rawHistory.removeLast();

        final float RANDOM_NUMBER = random.nextFloat();
        final int RAW_NEXT = rawMarkovChain.getNext(RANDOM_NUMBER, rawHistory);

        final S NEXT = states.get(RAW_NEXT);

        history.addFirst(NEXT);
        rawHistory.addFirst(RAW_NEXT);

        return NEXT;
    }

    /**
     * Returns the current state.
     * @return The current state.
     */
    public S current() {
        return history.get(0);
    }

    /**
     * Returns the previous state.
     * @return The previous state.
     */
    public S lookBack() {
        return history.get(1);
    }

    /**
     * Returns the nth previous state.
     * @param n How many states to look back.
     *          Input of 0 returns the current state.
     *          0 <= n < {@link #ORDER}.
     * @return the nth previous state.
     */
    public S lookBack(final int n) {
        final String ILLEGAL_N_MESSAGE = "Expected 0 <= n <= %s, received n = %s.";
        Preconditions.checkArgument(
                0 <= n && n <= ORDER,
                ILLEGAL_N_MESSAGE, ORDER, n
        );

        return history.get(n);
    }


    /**
     * The states of the Markov Chain.
     */
    public final ImmutableList<S> states;

    // private /////////////////////////////////////////////////////////

    private final LinkedList<S> history;
    private final LinkedList<Integer> rawHistory;

    private final Random random;
    private final RawMarkovChain rawMarkovChain;

    private final String ALL_STATES_UNIQUE_MESSAGE =
            "All objects in the state list should be unique.";

    private final static <S> boolean allUnique(List<S> objects) {
        Set<S> set = new HashSet<>(objects);
        return set.size() == objects.size();
    }
}
