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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.markovChains.dataStructures.ExplicitTransitionMatrix;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Tests {@link org.terasology.markovChains.MarkovChain}
 * @author Linus van Elswijk
 */
public class MarkovChainTest {
    // Markov Chain definition /////

    private static final ImmutableList<Character> STATES =
            ImmutableList.of('0', 'M', 'K', 'A', 'O', 'R', 'V');

    private static final float[][] TRANSITION_MATRIX_2D_RAW = {
            //   0  M  K  A  O  R  V
            {0, 1, 0, 0, 0, 0, 0}, //0 -> M
            {0, 0, 0, 1, 0, 0, 0}, //M -> A
            {0, 0, 0, 0, 1, 0, 0}, //K -> 0
            {0, 0, 0, 0, 0, 1, 0}, //A -> R
            {0, 0, 0, 0, 0, 0, 1}, //O -> V
            {0, 0, 1, 0, 0, 0, 0}, //R -> K
            {0, 0, 0, 0, 0, 0, 1}, //V -> V
    };

    private static final ExplicitTransitionMatrix TRANSITION_MATRIX_2D =
            new ExplicitTransitionMatrix(TRANSITION_MATRIX_2D_RAW);

    private static final float[][][] TRANSITION_MATRIX_3D_RAW = {
            TRANSITION_MATRIX_2D_RAW, //0
            TRANSITION_MATRIX_2D_RAW, //M
            TRANSITION_MATRIX_2D_RAW, //K
            TRANSITION_MATRIX_2D_RAW, //A
            TRANSITION_MATRIX_2D_RAW, //O
            TRANSITION_MATRIX_2D_RAW, //R
            TRANSITION_MATRIX_2D_RAW, //V
    };


    private static final ExplicitTransitionMatrix TRANSITION_MATRIX_3D =
            new ExplicitTransitionMatrix(TRANSITION_MATRIX_3D_RAW);

    /**
     * Tests a deterministic Markov Chain that generates the string "MARKOVVV". Markov Chain is constructed with various
     * constructors.
     */
    @Test
    public void deterministicChainTest() {

        final String expectedOutput = "MARKOVVV";

        List<MarkovChain<Character>> markovChains = ImmutableList.of(
                //Order 1
                new MarkovChain<>(STATES, TRANSITION_MATRIX_2D),
                new MarkovChain<>(STATES, TRANSITION_MATRIX_2D, 10983),
                new MarkovChain<>(STATES, TRANSITION_MATRIX_2D, new FastRandom()),

                //Order 2
                new MarkovChain<>(STATES, TRANSITION_MATRIX_3D),
                new MarkovChain<>(STATES, TRANSITION_MATRIX_3D, 46360),
                new MarkovChain<>(STATES, TRANSITION_MATRIX_3D, new FastRandom())
        );

        for (MarkovChain<Character> markovChain : markovChains) {
            deterministicChainTest(markovChain, expectedOutput);
        }
    }

    @Test
    public void setRandomTest() {
        MarkovChain<Character> chain = new MarkovChain<>(STATES, TRANSITION_MATRIX_2D);
        chain.next();

        MarkovChain<Character> chain2 = new MarkovChain<>(STATES, TRANSITION_MATRIX_2D);

        chain.setRandom(new FastRandom(456));
        chain.resetHistory();

        chain2.setRandom(new FastRandom(456));

        for (int i = 0; i < 5; i++) {
            assertEquals(chain.next(), chain2.next());
        }
    }

    private void deterministicChainTest(final MarkovChain<Character> markovChain, final String expectedOutput) {
        StringBuilder produced = new StringBuilder();

        for (int i = 0; i < expectedOutput.length(); i++) {
            // production
            produced.append(markovChain.next());
            // test current()
            assertEquals(produced.charAt(i), (char) markovChain.current());
            // test previous()
            if (i > 0) {
                assertEquals(produced.charAt(i - 1), (char) markovChain.previous());
            }
            //test previous(n)
            for (int j = 0; j < Math.min(i, markovChain.order); j++) {
                assertEquals(produced.charAt(i - j), (char) markovChain.previous(j));
            }
        }
        assertEquals(expectedOutput, produced.toString());
    }
}
