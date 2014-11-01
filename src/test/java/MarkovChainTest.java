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
import org.junit.Test;
import org.terasology.markovChains.MarkovChain;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;

public class MarkovChainTest {
    /**
     * Tests a deterministic Markov Chain that generates the string
     * "MARKOVVV"
     */
    @Test
    public void deterministicChainTest() {
        // Markov Chain definition /////
        List<Character> states = Arrays.asList('0', 'M', 'K', 'A', 'O', 'R', 'V');

        float [][] TRANSITION_MATRIX = {
        //   0  M  K  A  O  R  V
            {0, 1, 0, 0, 0, 0, 0}, //0 -> M
            {0, 0, 0, 1, 0, 0, 0}, //M -> A
            {0, 0, 0, 0, 1, 0, 0}, //K -> 0
            {0, 0, 0, 0, 0, 1, 0}, //A -> R
            {0, 0, 0, 0, 0, 0, 1}, //O -> V
            {0, 0, 1, 0, 0, 0, 0}, //R -> K
            {0, 0, 0, 0, 0, 0, 1}, //V -> V
        };
        final String EXPECTED = "MARKOVVV";

        MarkovChain<Character> markovChain =
                new MarkovChain<Character>(states, TRANSITION_MATRIX, 7357);

        // Test output /////////////////

        StringBuilder produced = new StringBuilder();
        for(int i = 0; i < EXPECTED.length(); i++) {
            produced.append(markovChain.next());

            assertEquals(produced.charAt(i), (char)markovChain.current());
            assertEquals(produced.charAt(i), (char)markovChain.lookBack(0));
            if(i > 0) {
                assertEquals(produced.charAt(i-1), (char)markovChain.lookBack());
                assertEquals(produced.charAt(i-1), (char)markovChain.lookBack(1));
            }
        }

        assertEquals(EXPECTED, produced.toString());
    }

}
