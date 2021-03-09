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
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.markovChains.dataStructures.TransitionMatrix;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * N-order Markov Chain implementation.
 * This is the user friendly version.
 *
 * If more control over the internal state is necessary, use {@link RawMarkovChain} instead.
 * @param <S> The type of the states.
 *
 * @since 1.00
 * @version 1.50
 */
public class MarkovChain<S> extends MarkovChainBase {

    // variables ////////////////////////////////////////////////////////

    private static final String ALL_STATES_UNIQUE_MESSAGE =
            "All objects in the state list should be unique.";

    /**
     * The states of the Markov Chain.
     */
    public final ImmutableList<S> states;

    /**
     * History of states ordered from least recent to most recent.
     * history.first() is the nth previous state, where n={@link #order}
     * history.last() is the current state.
     */
    private final List<S> history;

    /**
     * The state indices for the states in {@link #history},
     * such that for all x: states.get(rawHistory(x)) == history(x)
     */
    private final List<Integer> rawHistory;

    private final RawMarkovChain rawMarkovChain;

    private Random random;

    /**
     * Constructs a Markov Chain of any order and any set of states.
     *
     * <b>Note:</b> Avoid calling this constructor directly. Use the more user friendly, 2D and 3D array versions
     * whenever possible.
     *
     * @param states The states in the chain.
     * @param transitionMatrix The transition probabilities of length pow(nrOfStates, order + 1).
     *      The provided array should be a flattened n-dimensional array of probabilities, with
     *      n being the order of the markov chain.
     *
     * @since 1.50
     */
    public MarkovChain(List<S> states, TransitionMatrix transitionMatrix) {
        this(states, transitionMatrix, new FastRandom());
    }

    /**
     * Constructs a Markov Chain of any order and any set of states.
     *
     * <b>Note:</b> Avoid calling this constructor directly. Use the more user friendly, 2D and 3D array versions
     * whenever possible.
     *
     * @param states The states in the chain.
     * @param transitionMatrix The transition probabilities of length pow(nrOfStates, order + 1).
     *      The provided array should be a flattened n-dimensional array of probabilities, with
     *      n being the order of the markov chain.
     * @param seed The seed for the random number generator used for determining next states.
     *
     * @since 1.50
     */
    public MarkovChain(List<S> states, TransitionMatrix transitionMatrix, long seed) {
        this(states, transitionMatrix, new FastRandom(seed));
    }

    /**
     * Constructs a Markov Chain of any order and any set of states.
     *
     * <b>Note:</b> Avoid calling this constructor directly. Use the more user friendly, 2D and 3D array versions
     * whenever possible.
     *
     * @param states The states in the chain.
     * @param transitionMatrix The transition probabilities of length pow(nrOfStates, order + 1).
     *      The provided array should be a flattened n-dimensional array of probabilities, with
     *      n being the order of the markov chain.
     * @param random The random number generator used for determining next states.
     *
     * @since 1.50
     */
    public MarkovChain(List<S> states, TransitionMatrix transitionMatrix, Random random) {
        this(states, new RawMarkovChain(transitionMatrix), random);
    }

    /**
     * Wraps a MarkovChain interface over a {@link RawMarkovChain}.
     *
     *  <b>Note:</b> In most cases you won't need to construct a {@link RawMarkovChain} before constructing
     *  a {@link MarkovChain}. Use one of the other constructors where possible.
     *
     * @param states The states in the chain.
     * @param rawMarkovChain The RawMarkovChain controlling the MarkovChain.
     * @param random The random number generator used for determining next states.
     *
     * @since 1.50
     */
    public MarkovChain(List<S> states, RawMarkovChain rawMarkovChain, Random random) {
        super(rawMarkovChain.order, states.size());

        Preconditions.checkArgument(
                allUnique(states),
                ALL_STATES_UNIQUE_MESSAGE
        );

        this.states = ImmutableList.copyOf(states);
        this.rawMarkovChain = rawMarkovChain;
        this.random = random;
        this.history = new LinkedList<>();
        this.rawHistory = new LinkedList<>();

        resetHistory();
    }

    /**
     * Replace the RNG with a new one.
     *
     * @param random the new random number generator.
     *
     * @since 1.1.0
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    // public interface //////////////////////////////////////////////////

    /**
     * Moves the chain to the next state.
     *
     * @return The next state.
     *
     * @since 1.00
     */
    public S next() {
        history.remove(0);
        rawHistory.remove(0);

        final float randomNumber = random.nextFloat();
        final int rawNext = rawMarkovChain.getNext(randomNumber, rawHistory);

        final S next = states.get(rawNext);

        history.add(next);
        rawHistory.add(rawNext);

        return next;
    }

    /**
     * Returns the current state.
     * Equivalent to previous(0).
     *
     * @return The current state.
     *
     * @since 1.00
     */
    public S current() {
        return history.get(history.size() - 1);
    }

    /**
     * Returns the previous state.
     * Equivalent to previous(1).
     *
     * @return The previous state.
     *
     * @since 1.01
     */
    public S previous() {
        return history.get(history.size() - 2);
    }

    /**
     * Returns the nth previous state.
     * @param n How many states to look back.
     *          Input of 0 returns the current state.
     *          0 &le; n &lt; {@link #order}.
     * @return the nth previous state.
     *
     * @since 1.01
     */
    public S previous(final int n) {
        final String illegalNMessage = "Expected 0 <= n <= %s, received n = %s.";
        Preconditions.checkArgument(
                0 <= n && n <= order,
                illegalNMessage, order, n
        );

        return history.get(history.size() - n - 1);
    }

    /**
     * Resets the history.
     *
     * @since 1.00
     */
    public void resetHistory() {
        while (history.size() > 0) {
            history.remove(0);
            rawHistory.remove(0);
        }
        while (history.size() <= order) {
            history.add(states.get(0));
            rawHistory.add(0);
        }
    }

    // private /////////////////////////////////////////////////////////

    private static <S> boolean  allUnique(List<S> objects) {
        Set<S> set = new HashSet<>(objects);
        return set.size() == objects.size();
    }
}
