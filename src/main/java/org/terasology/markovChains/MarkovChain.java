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
 * @tparam S The type of the states.
 *
 * @since 31-10-2014
 * @author Linus van Elswijk
 */
public class MarkovChain<S> extends MarkovChainBase {

    public MarkovChain(int order, List<S> states, float[] transitionProbabilities) {
        this(order, states, transitionProbabilities, new FastRandom());
    }

    public MarkovChain(int order, List<S> states, float[] transitionProbabilities, long seed) {
        this(order, states, transitionProbabilities, new FastRandom(seed));
    }

    public MarkovChain(int order, List<S> states, float[] transitionProbabilities, Random random) {
        super(order, states.size());

        Preconditions.checkArgument(
                allUnique(states),
                "All objects in the state list should be unique."
        );

        this.states = ImmutableList.copyOf(states);
        this.rawMarkovChain = new RawMarkovChain(ORDER, NR_OF_STATES, transitionProbabilities);
        this.random = random;
        this.history = new LinkedList<S>();
        this.rawHistory = new LinkedList<Integer>();

        while(history.size() < order) {
            history.push(states.get(0));
            rawHistory.push(0);
        }
    }


    public S next() {
        float randomNumber = random.nextFloat();
        int rawNext = rawMarkovChain.getNext(randomNumber, rawHistory);

        S next = states.get(rawNext);

        history.removeFirst();
        rawHistory.removeFirst();
        history.push(next);
        rawHistory.push(rawNext);

        return next;
    }

    public S current() {
        return lookBack(0);
    }

    public S lookBack() {
        return lookBack(1);
    }

    public S lookBack(final int n) {
        final String ILLEGAL_N_MESSAGE = "Expected 0 <= n < %s, received n = %s.";
        Preconditions.checkArgument(
                1 <= n && n < ORDER,
                ILLEGAL_N_MESSAGE, ORDER, n
        );

        return history.get(history.size() - n - 1);
    }


    public final ImmutableList<S> states;


    private final LinkedList<S> history;
    private final LinkedList<Integer> rawHistory;

    private final Random random;
    private final RawMarkovChain rawMarkovChain;


    private final static <S> boolean allUnique(List<S> objects) {
        Set<S> set = new HashSet<>(objects);
        return set.size() == objects.size();
    }
}
