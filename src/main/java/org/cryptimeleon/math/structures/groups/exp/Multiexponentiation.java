package org.cryptimeleon.math.structures.groups.exp;

import org.cryptimeleon.math.structures.groups.GroupElementImpl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Mutable object describing a multiexponentiation as
 * constant * product(g_i^x_i).
 */
public class Multiexponentiation {
    ArrayList<MultiExpTerm> terms = null;
    GroupElementImpl constantFactor = null;

    public void put(MultiExpTerm term) {
        if (terms == null)
            terms = new ArrayList<>();
        terms.add(term);
    }

    public void put(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation) {
        put(new MultiExpTerm(base, exponent, precomputation));
    }

    public void put(GroupElementImpl groupelem) {
        constantFactor = constantFactor == null ? groupelem : constantFactor.op(groupelem);
    }

    /**
     * Ensures that all terms support the given window size by performing the precomputations that are necessary
     * to reach the desired window size for the interleaved sliding window algorithm.
     * @param windowSize The window size to ensure support for
     */
    public void ensurePrecomputation(int windowSize, MultiExpAlgorithm multiExpAlgorithm) {
        if (terms != null) {
            for (MultiExpTerm term : terms) {
                switch (multiExpAlgorithm) {
                    case SLIDING:
                        // if inversion is faster than op, we can potentially use existing precomputations of the
                        // other type
                        boolean invertExisting = term.getBase().getStructure().estimateCostInvPerOp() > 1;
                        if (term.getExponent().signum() < 0) {
                            term.getPrecomputation().computeNegativePowers(windowSize, invertExisting);
                        } else {
                            term.getPrecomputation().compute(windowSize, invertExisting);
                        }
                        break;
                    case WNAF:
                        if (term.getPrecomputation().getCurrentlySupportedWindowSize() < windowSize) {
                            // if we already have negative powers, we can just finish computing those
                            boolean precNegativePowers =
                                    (term.getPrecomputation().getCurrentlySupportedNegativeWindowSize()
                                    > term.getPrecomputation().getCurrentlySupportedPositiveWindowSize());
                            if (precNegativePowers) {
                                term.getPrecomputation().computeNegativePowers(windowSize, false);
                            } else {
                                term.getPrecomputation().compute(windowSize, false);
                            }
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported MultiExpAlgorithm " + multiExpAlgorithm);
                }

            }
        }
    }

    /**
     * Computes the minimum window size currently offered by the precomputations of all terms as required for
     * the given algorithm.
     * This is the window size such that all term's bases have cached precomputations of this size.
     * For the sliding algorithm, we only consider negative powers for negative exponents and positive powers
     * for positive exponents. wNAF may use both interchangeably.
     * @return The minimum window size supported by all terms.
     */
    public int computeMinPrecomputedWindowSize(MultiExpAlgorithm multiExpAlgorithm) {
        if (terms == null) {
            return 0;
        }
        int minPrecomputedWindowSize = Integer.MAX_VALUE;
        for (MultiExpTerm term : terms) {
            switch(multiExpAlgorithm) {
                case SLIDING:
                    if (term.getExponent().signum() >= 0) {
                        minPrecomputedWindowSize = Math.min(
                                minPrecomputedWindowSize,
                                term.precomputation == null ?
                                        0 : term.precomputation.getCurrentlySupportedPositiveWindowSize()
                        );
                    } else {
                        minPrecomputedWindowSize = Math.min(
                                minPrecomputedWindowSize,
                                term.precomputation == null ?
                                        0 : term.precomputation.getCurrentlySupportedNegativeWindowSize()
                        );
                    }
                    break;
                case WNAF:
                    minPrecomputedWindowSize = Math.min(
                            minPrecomputedWindowSize,
                            term.precomputation == null ? 0 : term.precomputation.getCurrentlySupportedWindowSize()
                    );
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported MultiExpAlgorithm " + multiExpAlgorithm);
            }
        }
        return minPrecomputedWindowSize;
    }

    public List<MultiExpTerm> getTerms() {
        return terms == null ? Collections.emptyList() : Collections.unmodifiableList(terms);
    }

    public int getNumberOfTerms() {
        return terms == null ? 0 : terms.size();
    }

    public Optional<GroupElementImpl> getConstantFactor() {
        return Optional.ofNullable(constantFactor);
    }

    public boolean isEmpty() {
        return (terms == null || terms.size() == 0) && constantFactor == null;
    }

    @Override
    public String toString() {
        StringBuilder stringRepr = new StringBuilder();
        stringRepr.append("Multiexponentiation:");
        terms.forEach(t -> stringRepr.append("\n").append(t.toString()));
        return stringRepr.toString();
    }
}
