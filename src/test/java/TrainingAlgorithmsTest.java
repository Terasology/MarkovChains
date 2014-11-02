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
import org.terasology.markovChains.TrainingAlgorithms;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by Linus on 2-11-2014.
 */
public class TrainingAlgorithmsTest {

    final char TERMINATOR = '\n';

    final public String[] trainingStrings = new String[]
            {
                    "abbabbabb",
                    "abababb",
                    "abbabbabb",
                    "ababbabababababb",
            };


    final static LinkedList<Character> states = new LinkedList<>();
    {
        states.add(TERMINATOR);

        Set<Character> encounteredChars = new HashSet<>();
        for(String string: trainingStrings) {
            for(char c: string.toCharArray()) {
                encounteredChars.add(c);
            }
        }

        states.addAll(encounteredChars);
    }

    private MarkovChain<Character> metalNameGenerator(int order) {

        Character[][] charSequences = new Character[trainingStrings.length][];
        for(int i = 0; i < charSequences.length; i++) {
            charSequences[i] = new Character[trainingStrings[i].length()];

            for(int j = 0; j < charSequences[i].length; j++) {
                charSequences[i][j] = trainingStrings[i].charAt(j);
            }
        }

        return TrainingAlgorithms.forwardAlgorithm(order, states, charSequences, TERMINATOR);
    }


    @Test
    public void generateNames() {
        System.out.println("Order 1 samples =======================");
        printNames(5, metalNameGenerator(1));

        System.out.println("Order 2 samples =======================");
        printNames(5, metalNameGenerator(2));

        System.out.println("Order 3 samples =======================");
        printNames(5, metalNameGenerator(3));

        System.out.println("Order 4 samples =======================");
        printNames(5, metalNameGenerator(4));
    }

    public void printNames(int NR_OF_NAMES, MarkovChain<Character> generator) {
        while(NR_OF_NAMES > 0) {
            generator.resetHistory();

            StringBuilder name = new StringBuilder();

            Character next = '0';
            while(next != TERMINATOR) {
                next = generator.next();
                if(next != TERMINATOR) {
                    name.append(next);
                }
            }

            boolean newS = true;
            for(int i = 0; i < trainingStrings.length; i++) {
                if(trainingStrings[i].equals(name.toString())) {
                    newS = false;
                }
            }
            if(newS) {
                System.out.println(name.toString());
                NR_OF_NAMES--;
            }
        }
    }

}
