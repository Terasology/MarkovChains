// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.markovChains;

import com.google.common.base.Preconditions;

/**
 * Abstract base class for Markov Chain implementations. Defines a minimal common interface.
 *
 * @version 1.50
 * @since 1.00
 */
public abstract class MarkovChainBase {

    // public //////////////////////////////////////////////////////////

    /**
     * The order of the Markov Chain
     *
     * @since 1.00
     */
    public final int order;

    /**
     * The number of states in the Markov Chain.
     *
     * @since 1.00
     */
    public final int nrOfStates;

    /**
     * Constructs a Markov Chain of any order and any nr of states.
     *
     * @param order The order (&ge; 1) of the Markov Chain, i.e. how many (previous) states are considered to
     *         compute the next.
     * @param nrOfStates The nr of states (&ge;1).
     * @since 1.00
     */
    protected MarkovChainBase(final int order, final int nrOfStates) {
        final String stateArgumentExceptionFormat = "nrOfStates=%s, should be >= 1";
        final String orderArgumentExceptionFormat = "order=%s, should be >= 1";

        Preconditions.checkArgument(
                nrOfStates > 0,
                stateArgumentExceptionFormat,
                nrOfStates
        );

        Preconditions.checkArgument(
                order > 0,
                orderArgumentExceptionFormat,
                order
        );

        this.order = order;
        this.nrOfStates = nrOfStates;
    }

}
