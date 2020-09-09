// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.markovChains.dataStructures;

/**
 * Interface for TransitionMatrix implementations that can be normalized.
 */
public interface Normalizable {

    /**
     * Normalizes the probability matrix. Will put the object into a state where isNormalized() returns true.
     *
     * @return this object
     * @since 1.50
     */
    TransitionMatrix normalize();
}
