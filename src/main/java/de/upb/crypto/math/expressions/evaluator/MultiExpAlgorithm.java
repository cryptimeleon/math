package de.upb.crypto.math.expressions.evaluator;

/**
 * Contains the different multi-exponentiation algorithms that are implemented in {@link MultiExpAlgorithms}.
 */
public enum MultiExpAlgorithm {
    INTERLEAVED_SLIDING, INTERLEAVED_WNAF, SIMULTANEOUS
}
