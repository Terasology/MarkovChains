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

import org.junit.Test;
import org.terasology.markovChains.dataStructures.ExplicitTransitionMatrix;
import org.terasology.markovChains.dataStructures.TransitionMatrix;
import org.terasology.math.TeraMath;

import java.util.Deque;
import java.util.LinkedList;

import static junit.framework.Assert.*;

/**
 * Tests {@link org.terasology.markovChains.RawMarkovChain}
 * @author Linus van Elswijk
 */
public class RawMarkovChainTest {

    /**
     * Tests deterministic Markov Chains that always skip n states,
     * for various values of n, orders and nr of states
     */
    @Test
    public void skipNTest() {
        skipNTest(false);
    }

    /**
     * Tests Markov Chains that never skip n states,
     * for various values of n, orders and nr of states
     */
    @Test
    public void doNotSkipNTest() {
        skipNTest(true);
    }

    /**
     * Tests if the constructors produce the expected Markov Chains.
     * Also tests whether or not probabilities get normalized correctly.
     */
    @Test
    public void constructorAndNormalizationTest() {
        constructorTest(1, 17);
        constructorTest(2, 9);
        constructorTest(3, 5);
        constructorTest(9, 2);
    }

    /**
     * Tests user friendly wrappers of methods:
     *  * Constructor accepting a 2D array
     *  * Constructor accepting a 3D array
     */
    @Test
    public void testSugar() {
        // shared data ////////////////////
        final float maxError = 1.0e-4f;

        // Test 2D matrix constructor /////
        final float[][] matrix2D = new float [][] {
                {0.0f, 0.1f, 0.2f, 0.3f},
                {1.0f, 1.1f, 1.2f, 1.3f},
                {2.0f, 2.1f, 2.2f, 2.3f},
                {3.0f, 3.1f, 3.2f, 3.3f}
        };

        RawMarkovChain firstOrder = new RawMarkovChain(new ExplicitTransitionMatrix(matrix2D));

        assertTrue(TeraMath.fastAbs(firstOrder.getTransitionMatrix().get(0, 0) - matrix2D[0][0]) < maxError);
        assertTrue(TeraMath.fastAbs(firstOrder.getTransitionMatrix().get(2, 1) - matrix2D[2][1]) < maxError);
        assertTrue(TeraMath.fastAbs(firstOrder.getTransitionMatrix().get(3, 3) - matrix2D[3][3]) < maxError);


        // Test 3D matrix constructor /////

        final float[][][] matrix3D = new float[][][] {
                new float[][] {
                        {0.00f, 0.01f, 0.02f},
                        {0.10f, 0.11f, 0.12f},
                        {0.20f, 0.21f, 0.22f},
                },

                new float[][] {
                        {1.00f, 1.01f, 1.02f},
                        {1.10f, 1.11f, 1.12f},
                        {1.20f, 1.21f, 1.22f},
                },

                new float[][] {
                        {2.00f, 2.01f, 2.02f},
                        {2.10f, 2.11f, 2.12f},
                        {2.20f, 2.21f, 2.22f},
                },
        };

        RawMarkovChain secondOrder = new RawMarkovChain(new ExplicitTransitionMatrix(matrix3D));

        assertTrue(TeraMath.fastAbs(secondOrder.getTransitionMatrix().get(0, 0, 0) - matrix3D[0][0][0]) < maxError);
        assertTrue(TeraMath.fastAbs(secondOrder.getTransitionMatrix().get(2, 0, 1) - matrix3D[2][0][1]) < maxError);
        assertTrue(TeraMath.fastAbs(secondOrder.getTransitionMatrix().get(2, 2, 2) - matrix3D[2][2][2]) < maxError);
    }

    /**
     * Tests if the distribution of next() is the same as the transition matrix would suggest.
     */
    @Test
    public void testNextDistribution() {
        final RawMarkovChain rawMarkovChain = new RawMarkovChain(TestUtilities.randomTransitionMatrix(3, 4));
        rawMarkovChain.getTransitionMatrix().normalize();

        final int nrOfSamples = 1000;

        nextDistributionTest(rawMarkovChain, nrOfSamples, 0, 0, 0);
        nextDistributionTest(rawMarkovChain, nrOfSamples, 1, 3, 2);
        nextDistributionTest(rawMarkovChain, nrOfSamples, 3, 2, 2);
        nextDistributionTest(rawMarkovChain, nrOfSamples, 3, 3, 3);
    }

    private void nextDistributionTest(RawMarkovChain markovChain, int nrOfSamples, int... history) {
        int[] hits = new int[4];

        for (int i = 0; i < nrOfSamples; i++) {
            hits[markovChain.getNext(TestUtilities.randomNumberGenerator.nextFloat(), history)]++;
        }

        for (int i = 0; i < 4; i++) {
            float expected = markovChain.getTransitionMatrix().get(
                    history[0],
                    history[1],
                    history[2],
                    i);
            float actual = ((float) hits[i]) / nrOfSamples;
            assertTrue(TeraMath.fastAbs(expected - actual) < 0.125f);
        }
    }

    private void constructorTest(final int order, final int states) {
        final String failureLocationFormat = "order=%s , states=%s , i=%s ::";
        /*
        final float[][] probabilities = randomTransitionArray(order, states);
        final ExplicitTransitionMatrix constructorInput = new ExplicitTransitionMatrix(order, states, probabilities[0]);
        final float[] normalizedInput = probabilities[1];
        final float maxError = 1.0e-4f;

        RawMarkovChain markovChain = new RawMarkovChain(constructorInput);
        for (int i = 0; i < constructorInput.length; i++) {
            int[] stateArray = indexToStates(order, states, i);
            assertTrue(
                String.format(failureLocationFormat, order, states, i) +
                " expected: " + constructorInput[i] +
                ", actual: " + markovChain.getTransitionMatrix().get(stateArray),
                TeraMath.fastAbs(constructorInput[i] - markovChain.getTransitionMatrix().get(stateArray)) < maxError
            );
        }

        markovChain.getTransitionMatrix().normalize();
        for (int i = 0; i < probabilities[1].length; i++) {
            int[] stateArray = indexToStates(order, states, i);
            assertTrue(
                String.format(failureLocationFormat, order, states, i) +
                " expected: " + normalizedInput[i] +
                ", actual: " + markovChain.getTransitionMatrix().get(stateArray),
                TeraMath.fastAbs(normalizedInput[i] - markovChain.getTransitionMatrix().get(stateArray)) < maxError
            );
        }*/
    }

    private void skipNTest(final boolean doNotSkipN) {
        // first order Markov Chains //////////////////////
        skipNTest(1, 13,  0, doNotSkipN);  //skip none : return current
        skipNTest(1, 13,  1, doNotSkipN);  //skip 1    : return current+1
        skipNTest(1, 13, 12, doNotSkipN);  //skip 12   : return current-1

        // second order Markov Chains /////////////////////
        skipNTest(2, 7, 0, doNotSkipN);   //skip none : return current
        skipNTest(2, 7, 1, doNotSkipN);   //skip 1    : return current+1
        skipNTest(2, 7, 6, doNotSkipN);   //skip 6    : return current-1

        // 5th order Markov Chains ////////////////////////
        skipNTest(5, 5, 0, doNotSkipN);  //skip none  : return current
        skipNTest(5, 5, 1, doNotSkipN);  //skip 1     : return current+1
        skipNTest(5, 5, 6, doNotSkipN);  //skip 12    : return current-1

        // 7th order Markov Chains ////////////////////////
        skipNTest(7, 3, 0, doNotSkipN);  //skip none  : return current
        skipNTest(7, 3, 1, doNotSkipN);  //skip 1     : return current+1
        skipNTest(7, 3, 2, doNotSkipN);  //skip 2     : return current-1
    }

    private void skipNTest(final int order, final int states, final int n, final boolean doNotSkipN) {
        float[] props2A = doNotSkipN
                ? TestUtilities.doNotSkipNTransitionArray(n, order, states)
                : TestUtilities.skipNTransitionArray(n, order, states);

        TransitionMatrix transitionMatrix = new ExplicitTransitionMatrix(order, states, props2A);

        RawMarkovChain chain = new RawMarkovChain(transitionMatrix);
        chain.getTransitionMatrix().normalize();

        Deque<Integer> previousStates = new LinkedList<>();

        for (int i = 0; i < order; i++) {
            previousStates.push(0);
        }

        final int nrOfRuns = doNotSkipN ? states * 2 : states;
        for (int i = 0; i < nrOfRuns; i++) {
            int next = chain.getNext(TestUtilities.randomNumberGenerator.nextFloat(), previousStates);
            if (doNotSkipN) {
                assertFalse((previousStates.getLast() + n) % states == next);
            } else {
                assertEquals((previousStates.getLast() + n) % states, next);
            }

            previousStates.removeFirst();
            previousStates.push(next);
        }
    }

    private int[] indexToStates(final int order, final int nrOfStates, int index) {
        int[] stateArray = new int[order + 1];
        int currentPow = 1;
        int nextPower = nrOfStates;

        for (int ord = 0; ord <= order; ord++, currentPow = nextPower, nextPower *= nrOfStates) {
            stateArray[stateArray.length - ord - 1] = (index % nextPower) / currentPow;
        }

        return stateArray;
    }
}
