import org.junit.Test;
import org.terasology.markovChains.RawMarkovChain;
import org.terasology.math.TeraMath;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;

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
    public void ConstructorTest() {
        ConstructorTest(1, 17);
        ConstructorTest(2, 9);
        ConstructorTest(3, 5);
        ConstructorTest(9, 2);
    }

    private void ConstructorTest(final int order, final int states) {
        final float[][] probabilities = randomTransitionArray(order, states);
        final float MAX_ERROR = 1.0e-4f;

        RawMarkovChain markovChain = new RawMarkovChain(order, states, probabilities[0]);
        for(int i = 0; i < probabilities[0].length; i++) {
            int[] stateArray = indexToStates(order, states, i);
            assert(TeraMath.fastAbs(probabilities[1][i] - markovChain.getProbability(stateArray)) < MAX_ERROR );
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
            stateArray[ord] = index / currentPow % nextPower;
        }

        return stateArray;
    }
}
