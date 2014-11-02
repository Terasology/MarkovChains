import org.junit.Test;
import org.terasology.markovChains.RawMarkovChain;
import org.terasology.math.TeraMath;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.lang.reflect.Method;
import java.util.LinkedList;

import static junit.framework.Assert.*;

public class RawMarkovChainTest {
    Random randomNumberGenerator = new FastRandom(7357);

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
    public void ConstructorAndNormalizationTest() {
        ConstructorTest(1, 17);
        ConstructorTest(2, 9);
        ConstructorTest(3, 5);
        ConstructorTest(9, 2);
    }

    /**
     * Tests the isNormalized method.
     */
    @Test
    public void TestIsNormalized() {
        RawMarkovChain chain = new RawMarkovChain(1, 4, randomTransitionArray(1, 4)[0]);

        assertFalse(chain.isNormalized());
        chain.normalizeProbabilities();
        assertTrue(chain.isNormalized());
        chain.setProbability(0.5f, 1, 3);
        assertFalse(chain.isNormalized());
        chain.normalizeProbabilities();
        assertTrue(chain.isNormalized());
        chain.normalizeProbabilities();
        assertTrue(chain.isNormalized());
    }

    /**
     * Tests user friendly wrappers of methods:
     *  * Constructor accepting a 2D array
     *  * Constructor accepting a 3D array
     */
    @Test
    public void testSugar()
    {
        // shared data ////////////////////
        final float MAX_ERROR = 1.0e-4f;

        // Test 2D matrix constructor /////
        {
            final float[][] MATRIX_2D = {
                    {0.0f, 0.1f, 0.2f, 0.3f},
                    {1.0f, 1.1f, 1.2f, 1.3f},
                    {2.0f, 2.1f, 2.2f, 2.3f},
                    {3.0f, 3.1f, 3.2f, 3.3f}
            };

            RawMarkovChain firstOrder = new RawMarkovChain(MATRIX_2D);

            assertTrue(TeraMath.fastAbs(firstOrder.getProbability(0, 0) - MATRIX_2D[0][0]) < MAX_ERROR);
            assertTrue(TeraMath.fastAbs(firstOrder.getProbability(2, 1) - MATRIX_2D[2][1]) < MAX_ERROR);
            assertTrue(TeraMath.fastAbs(firstOrder.getProbability(3, 3) - MATRIX_2D[3][3]) < MAX_ERROR);
        }

        // Test 3D matrix constructor /////
        {
            final float[][][] MATRIX_3D = {
                {   {0.00f, 0.01f, 0.02f},
                    {0.10f, 0.11f, 0.12f},
                    {0.20f, 0.21f, 0.22f},
                },

                {   {1.00f, 1.01f, 1.02f},
                    {1.10f, 1.11f, 1.12f},
                    {1.20f, 1.21f, 1.22f},
                },

                {   {2.00f, 2.01f, 2.02f},
                    {2.10f, 2.11f, 2.12f},
                    {2.20f, 2.21f, 2.22f},
                },
            };

            RawMarkovChain firstOrder = new RawMarkovChain(MATRIX_3D);

            assertTrue(TeraMath.fastAbs(firstOrder.getProbability(0, 0, 0) - MATRIX_3D[0][0][0]) < MAX_ERROR);
            assertTrue(TeraMath.fastAbs(firstOrder.getProbability(2, 0, 1) - MATRIX_3D[2][0][1]) < MAX_ERROR);
            assertTrue(TeraMath.fastAbs(firstOrder.getProbability(2, 2, 2) - MATRIX_3D[2][2][2]) < MAX_ERROR);
        }
    }

    /**
     * Tests if an exception is thrown for an invalid state input
     */
    @Test
    public void invalidStateInputTest() {
        RawMarkovChain chain = new RawMarkovChain(1, 4, randomTransitionArray(1, 4)[0]);
        chain.normalizeProbabilities();

        try {
            chain.getProbability(0, 4);
            fail("Exception should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // test passed
        }

        try {
            chain.getProbability(-1, 3);
            fail("Exception should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // test passed
        }
    }

    /**
     * Tests if the normalization correctly handles
     * a transition matrix with only zeros.
     */
    @Test
    public void testZeroMatrixNormalization() {
        final float[][] TRANSITION_MATRIX = {
            {0f, 0f, 0f},
            {0f, 0f, 0f},
            {0f, 0f, 0f}
        };

        RawMarkovChain chain = new RawMarkovChain(TRANSITION_MATRIX);
        chain.normalizeProbabilities();

        final float EXPECTED = 1.0f / 3;
        final float EPSILON = 1.0e-5f;
        assertTrue(TeraMath.fastAbs(chain.getProbability(0, 0) - EXPECTED) < EPSILON);
        assertTrue(TeraMath.fastAbs(chain.getProbability(1,2) - EXPECTED) < EPSILON);
        assertTrue(TeraMath.fastAbs(chain.getProbability(2,2) - EXPECTED) < EPSILON);
    }

    /**
     * Tests if the distribution of next() is the same as the transition matrix would suggest.
     */
    @Test
    public void testNextDistribution() {
        final RawMarkovChain rawMarkovChain = new RawMarkovChain(3, 4, randomTransitionArray(3,4)[0]);
        rawMarkovChain.normalizeProbabilities();

        final int NR_OF_SAMPLES = 1000;

        testNextDistribution(rawMarkovChain, NR_OF_SAMPLES, 0, 0, 0);
        testNextDistribution(rawMarkovChain, NR_OF_SAMPLES, 1, 3, 2);
        testNextDistribution(rawMarkovChain, NR_OF_SAMPLES, 3, 2, 2);
        testNextDistribution(rawMarkovChain, NR_OF_SAMPLES, 3, 3, 3);
    }

    private void testNextDistribution(RawMarkovChain markovChain, int NR_OF_SAMPLES, int ... history) {
        int[] hits = new int[4];

        for(int i = 0; i < NR_OF_SAMPLES; i++) {
            hits[markovChain.getNext(randomNumberGenerator.nextFloat(), history)]++;
        }

        for(int i = 0; i < 4; i++) {
            float expected = markovChain.getProbability(
                    history[0],
                    history[1],
                    history[2],
                    i);
            float actual = ((float)hits[i]) / NR_OF_SAMPLES;
            assertTrue(TeraMath.fastAbs(expected - actual) < 0.125f);
        }
    }

    @Test
    public void testPrivateToIndex() {
        RawMarkovChain markovChain = new RawMarkovChain(3, 4, skipNTransitionArray(1, 3, 4));

        try {
            Method method = RawMarkovChain.class.getDeclaredMethod("toIndex", int[].class);
            method.setAccessible(true);

            assertEquals(  0, method.invoke(markovChain, new int[]{0,0,0,0}));
            assertEquals(  1, method.invoke(markovChain, new int[]{0,0,0,1}));
            assertEquals(  4, method.invoke(markovChain, new int[]{0,0,1,0}));
            assertEquals( 16, method.invoke(markovChain, new int[]{0,1,0,0}));
            assertEquals( 64, method.invoke(markovChain, new int[]{1,0,0,0}));
            assertEquals( 85, method.invoke(markovChain, new int[]{1,1,1,1}));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void ConstructorTest(final int order, final int states) {
        final String FAILURE_LOCATION_FORMAT = "order=%s , states=%s , i=%s ::";

        final float[][] probabilities = randomTransitionArray(order, states);
        final float[] constructorInput = probabilities[0];
        final float[] normalizedInput = probabilities[1];
        final float MAX_ERROR = 1.0e-4f;

        RawMarkovChain markovChain = new RawMarkovChain(order, states, constructorInput);
        for(int i = 0; i < constructorInput.length; i++) {
            int[] stateArray = indexToStates(order, states, i);
            assertTrue(
                String.format(FAILURE_LOCATION_FORMAT, order, states, i) +
                " expected: " + constructorInput[i] +
                ", actual: " + markovChain.getProbability(stateArray),
                TeraMath.fastAbs(constructorInput[i] - markovChain.getProbability(stateArray)) < MAX_ERROR
            );
        }

        markovChain.normalizeProbabilities();
        for(int i = 0; i < probabilities[1].length; i++) {
            int[] stateArray = indexToStates(order, states, i);
            assertTrue(
                String.format(FAILURE_LOCATION_FORMAT, order, states, i) +
                " expected: " + normalizedInput[i] +
                ", actual: " + markovChain.getProbability(stateArray),
                TeraMath.fastAbs(normalizedInput[i] - markovChain.getProbability(stateArray)) < MAX_ERROR
            );
        }
    }

    private void skipNTest(final boolean doNotSkipN) {
        // first order Markov Chains //////////////////////
        skipNTest(1,13,0, doNotSkipN);  //skip none : return current
        skipNTest(1,13,1, doNotSkipN);  //skip 1    : return current+1
        skipNTest(1,13,12, doNotSkipN); //skip 12   : return current-1

        // second order Markov Chains /////////////////////
        skipNTest(2,7,0, doNotSkipN);   //skip none : return current
        skipNTest(2,7,1, doNotSkipN);   //skip 1    : return current+1
        skipNTest(2,7,6, doNotSkipN);   //skip 6    : return current-1

        // 5th order Markov Chains ////////////////////////
        skipNTest(5,5,0, doNotSkipN);  //skip none  : return current
        skipNTest(5,5,1, doNotSkipN);  //skip 1     : return current+1
        skipNTest(5,5,6, doNotSkipN);  //skip 12    : return current-1

        // 7th order Markov Chains ////////////////////////
        skipNTest(7,3,0, doNotSkipN);  //skip none  : return current
        skipNTest(7,3,1, doNotSkipN);  //skip 1     : return current+1
        skipNTest(7,3,2, doNotSkipN);  //skip 2     : return current-1
    }

    private void skipNTest(final int order, final int states, final int n, final boolean doNotSkipN) {
        float[] props2A = doNotSkipN ?
                doNotSkipNTransitionArray(n, order, states):
                    skipNTransitionArray(n, order, states);

        RawMarkovChain chain = new RawMarkovChain(order, states, props2A);
        chain.normalizeProbabilities();

        LinkedList<Integer> previousStates = new LinkedList<>();

        for(int i = 0; i< order; i++) {
            previousStates.push(0);
        }

        {
            final int NR_OF_RUNS = doNotSkipN ? states * 2 : states;
            for (int i = 0; i < NR_OF_RUNS; i++) {
                int next = chain.getNext(randomNumberGenerator.nextFloat(), previousStates);
                if (doNotSkipN) {
                    assertFalse((previousStates.getLast() + n) % states == next);
                } else {
                    assertEquals((previousStates.getLast() + n) % states, next);
                }

                previousStates.removeFirst();
                previousStates.push(next);
            }
        }
    }

    private float[][] randomTransitionArray(final int order, final int states) {
        float[][] probabilities = new float[2][TeraMath.pow(states,order + 1)];

        for(int i = 0; i < probabilities[0].length; i+= states) {
            float[] iProbabilities = new float[states];
            float sumOfProbabilities = 0f;
            float sum = 0;

            for(int state = 0; state < states; state++) {
                iProbabilities[state] = randomNumberGenerator.nextFloat();
                sumOfProbabilities += iProbabilities[state];
            }

            for(int state = 0; state < states; state++) {
                probabilities[0][i + state] = iProbabilities[state];
                probabilities[1][i + state] = iProbabilities[state] / sumOfProbabilities;
                sum += probabilities[1][i + state];
            }
        }

        return probabilities;
    }

    private float[] skipNTransitionArray(final int n, final int order, final int states) {
        float[] probabilities = new float[TeraMath.pow(states,order + 1)];

        int CYCLE_LENGTH = states * states;

        for(int i = 0; i < probabilities.length; i++) {
            final int PREVIOUS_STATE = i % CYCLE_LENGTH / states;
            final int CURRENT_STATE = i % states;

            if( (PREVIOUS_STATE + n) % states == CURRENT_STATE ) {
                probabilities[i] = 1f;
            }
            else {
                probabilities[i] = 0f;
            }
        }

        return probabilities;
    }

    private float[] doNotSkipNTransitionArray(final int n, final int order, final int states) {
        float[] probabilities = skipNTransitionArray(n,order,states);
        for(int i = 0; i < probabilities.length; i++) {
            probabilities[i] = 1.0f - probabilities[i];
        }

        return probabilities;
    }

    private int[] indexToStates(final int order, final int nrOfStates, int index) {
        int[] stateArray = new int[order + 1];
        int currentPow = 1;
        int nextPower = nrOfStates;

        for(int ord = 0; ord <= order; ord++, currentPow = nextPower, nextPower *= nrOfStates) {
            stateArray[stateArray.length - ord - 1] = (index % nextPower) / currentPow ;
        }

        return stateArray;
    }
}
