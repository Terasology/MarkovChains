// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.markovChains;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Tests {@link TrainingAlgorithms}.
 */
public class TrainingAlgorithmsTest {

    private static final char TERMINAL_CHAR = '\n';

    private static final Pattern[] EXPECTED_PATTERN = new Pattern[]{
            Pattern.compile("(^a(a|b)*b$)"),
            Pattern.compile("(^ab(a|b)*bb$)" + "|" + "(^abb$)"),
            Pattern.compile("(^ab(a|b)*abb$)" + "|" + "(^abb$)")
    };

    private static final String[] TRAINING_STRINGS = new String[]{
            "abbabbabb",
            "abababb",
            "abbabbabb",
            "ababbabababababb",
    };

    private static final List<Character> STATES = new LinkedList<>();

    static {   // fill STATES
        STATES.add(TERMINAL_CHAR);

        Set<Character> encounteredChars = new HashSet<>();
        for (String string : TRAINING_STRINGS) {
            for (char c : string.toCharArray()) {
                encounteredChars.add(c);
            }
        }

        STATES.addAll(encounteredChars);
    }

    @Test
    public void generateStrings() {
        checkStrings(5, stringGenerator(1));
        checkStrings(5, stringGenerator(2));
        checkStrings(5, stringGenerator(3));
    }

    public void checkStrings(final int nrOfNames, final MarkovChain<Character> generator) {
        for (int i = 0; i < nrOfNames; i++) {
            generator.resetHistory();

            StringBuilder name = new StringBuilder();

            Character next = '0';
            while (next != TERMINAL_CHAR) {
                next = generator.next();
                if (next != TERMINAL_CHAR) {
                    name.append(next);
                }
            }

            checkString(generator.order, name.toString());
        }
    }

    private void checkString(final int order, final String string) {
        final Pattern pattern = EXPECTED_PATTERN[order - 1];

        String message = String.format(
                "String %s from generator of order %d should match pattern \"%s\"",
                string, order, pattern
        );

        assertTrue(message, EXPECTED_PATTERN[order - 1].matcher(string).matches());
    }

    private MarkovChain<Character> stringGenerator(int order) {
        Character[][] charSequences = new Character[TRAINING_STRINGS.length][];
        for (int i = 0; i < charSequences.length; i++) {
            charSequences[i] = new Character[TRAINING_STRINGS[i].length()];

            for (int j = 0; j < charSequences[i].length; j++) {
                charSequences[i][j] = TRAINING_STRINGS[i].charAt(j);
            }
        }

        return TrainingAlgorithms.forwardAlgorithm(order, STATES, Arrays.asList(charSequences), TERMINAL_CHAR);
    }
}
