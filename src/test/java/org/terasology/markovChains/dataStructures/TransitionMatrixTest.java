// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.markovChains.dataStructures;

import junit.framework.Assert;
import org.junit.Test;
import org.terasology.markovChains.TestUtilities;
import org.terasology.math.TeraMath;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Tests {@link org.terasology.markovChains.dataStructures.TransitionMatrix}
 */
public class TransitionMatrixTest {

    /**
     * Test set multiple
     */
    @Test
    public void setRowTest() {
        ExplicitTransitionMatrix matrix = TestUtilities.randomTransitionMatrix(2, 4);

        matrix.normalize();

        float[] probsIncorrectLength = {1, 2, 3};
        float[] probsInvalid = {0.0f, -0.2f, 0.3f, 0.5f};
        float[] probsValid = {0.0f, 0.2f, 0.3f, 0.5f};

        int[] statesIncorrectLength = {1, 1, 2};
        int[] statesInvalid = {1, 4};
        int[] statesValid = {1, 2};

        try {
            matrix.setRow(probsIncorrectLength, statesValid);
            Assert.fail("Exception should have been thrown");

            // Avoid unused local value warnings.
            matrix.get(statesValid);
        } catch (IllegalArgumentException e) {
            // test passed
            assertTrue(true);
        }

        try {
            matrix.setRow(probsInvalid, statesValid);
            Assert.fail("Exception should have been thrown");

            // Avoid unused local value warnings.
            matrix.get(statesValid);
        } catch (IllegalArgumentException e) {
            // test passed
            assertTrue(true);
        }

        try {
            matrix.setRow(probsValid, statesIncorrectLength);
            Assert.fail("Exception should have been thrown");

            // Avoid unused local value warnings.
            matrix.get(statesValid);
        } catch (IllegalArgumentException e) {
            // test passed
            assertTrue(true);
        }

        try {
            matrix.setRow(probsValid, statesInvalid);
            Assert.fail("Exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // test passed
            assertTrue(true);
        }

        matrix.setRow(probsValid, statesValid);
        assertFalse(matrix.isNormalized());

        final float epsilon = 1.0e-5f;
        for (int i = 0; i < 4; i++) {
            float expected = probsValid[i];
            float actual = matrix.get(statesValid[0], statesValid[1], i);
            assertTrue(TeraMath.fastAbs(actual - expected) < epsilon);
        }
    }
}
