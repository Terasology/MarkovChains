/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.markovChains.dataStructures;

import junit.framework.Assert;
import org.junit.Test;
import org.terasology.markovChains.TestUtilities;
import org.terasology.math.TeraMath;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests {@link org.terasology.markovChains.dataStructures.ExplicitTransitionMatrix}
 * @author Linus van Elswijk
 */
public class ExplicitTransitionMatrixTest {

    /**
     * Checks that the createTransitionArray creates an array of the right size and
     * that it only contains zeros.
     */
    @Test
    public void testCreateTransitionArray() {
        final int order = 0;
        final int nrOfStates = 1;
        final int[][] orderAndStateNrPairs = {
                {1, 1},
                {1, 9},
                {3, 4},
                {5, 3}
        };

        for (int[] param : orderAndStateNrPairs) {
            try {
                TransitionMatrix matrix = new ExplicitTransitionMatrix(
                        param[order], param[nrOfStates], ExplicitTransitionMatrix.createTransitionArray(param[order], param[nrOfStates])
                );
            } catch (Exception e) {
                fail("Constructor threw an exception / transition array was not accepted.");
            }
        }
    }

    /**
     * Tests if the normalization correctly handles
     * a transition matrix with only zeros.
     */
    @Test
    public void testMatrixNormalization() {
        final float epsilon = 1.0e-5f;

        //test zero normalization
        final float[][] zeroTransitionMatrix = new float[][] {
                {0f, 0f, 0f},
                {0f, 0f, 0f},
                {0f, 0f, 0f}
        };

        ExplicitTransitionMatrix matrix = new ExplicitTransitionMatrix(zeroTransitionMatrix);
        matrix.normalize();

        final float expected = 1.0f / 3;
        assertTrue(TeraMath.fastAbs(matrix.get(0, 0) - expected) < epsilon);
        assertTrue(TeraMath.fastAbs(matrix.get(1, 2) - expected) < epsilon);
        assertTrue(TeraMath.fastAbs(matrix.get(2, 2) - expected) < epsilon);

        final float[][] randomTransitionArray = TestUtilities.randomTransitionArray(1,4);
        ExplicitTransitionMatrix randomMatrix = new ExplicitTransitionMatrix(1,4, randomTransitionArray[0]);
        ExplicitTransitionMatrix randomMatrixNormalized = new ExplicitTransitionMatrix(1,4,randomTransitionArray[1]);
        randomMatrix.normalize();

        for(int x = 0; x < 4; x++) {
            for(int y = 0; y < 4; y++) {
                assertTrue(TeraMath.fastAbs(randomMatrix.get(x, y) - randomMatrixNormalized.get(x,y)) < epsilon);
            }
        }
    }

    /**
     * Tests if an exception is thrown for an invalid state input
     */
    @Test
    public void invalidStateInputTest() {
        ExplicitTransitionMatrix matrix = TestUtilities.randomTransitionMatrix(1, 4);
        matrix.normalize();

        try {
            matrix.get(0, 4);
            Assert.fail("Exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // test passed
            assertTrue(true);
        }

        try {
            matrix.get(-1, 3);
            Assert.fail("Exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // test passed
            assertTrue(true);
        }
    }
}
