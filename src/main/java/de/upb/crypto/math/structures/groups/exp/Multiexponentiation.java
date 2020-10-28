package de.upb.crypto.math.structures.groups.exp;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

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
    int minPrecomputedWindowSize = 0;

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
     * Terms with negative exponents will have the precomputation done for negative powers.
     * @param windowSize The window size to ensure support for
     */
    public void ensureSlidingPrecomputation(int windowSize) {
        if (terms != null) {
            for (MultiExpTerm term : terms) {
                if (term.getExponent().signum() < 0) {
                    term.getPrecomputation().computeNegativePowers(windowSize);
                } else {
                    term.getPrecomputation().compute(windowSize);
                }
            }
        }
    }

    /**
     * Ensures that all terms support the given window size by performing the precomputations that are necessary
     * to reach the desired window size for the interleaved wNAF algorithm.
     * wNAF does not care whether positive or negative powers are available, but it will compute positive powers
     * if neither exist already.
     * @param windowSize The window size to ensure support for
     */
    public void ensureWNafPrecomputation(int windowSize) {
        if (terms != null) {
            for (MultiExpTerm term : terms) {
                if (term.getPrecomputation().getCurrentlySupportedWindowSize() < windowSize) {
                    // if we already have negative powers, we can just finish computing those
                    boolean precNegativePowers = (term.getPrecomputation().getCurrentlySupportedNegativeWindowSize()
                                    > term.getPrecomputation().getCurrentlySupportedPositiveWindowSize());
                    if (precNegativePowers) {
                        term.getPrecomputation().computeNegativePowers(windowSize);
                    } else {
                        term.getPrecomputation().compute(windowSize);
                    }
                }
            }
        }
    }

    /**
     * Computes the minimum window size currently offered by the precomputations of all terms as required for
     * the interleaved sliding window algorithm.
     * This is the window size such that all term's bases have cached precomputations of this size.
     * Terms with negative exponents will have their negative power precomputation window size used
     * for this computation.
     * @return The minimum window size supported by all terms.
     */
    public int computeSlidingMinPrecomputedWindowSize() {
        if (terms == null) {
            return 0;
        }
        int minPrecomputedWindowSize = Integer.MAX_VALUE;
        for (MultiExpTerm term : terms) {
            if (term.getExponent().signum() >= 0) {
                minPrecomputedWindowSize = Math.min(
                        minPrecomputedWindowSize,
                        term.precomputation == null ? 0 : term.precomputation.getCurrentlySupportedPositiveWindowSize()
                );
            } else {
                minPrecomputedWindowSize = Math.min(
                        minPrecomputedWindowSize,
                        term.precomputation == null ? 0 : term.precomputation.getCurrentlySupportedNegativeWindowSize()
                );
            }
        }
        return minPrecomputedWindowSize;
    }

    /**
     * Computes the minimum window size currently offered by the precomputations of all terms as required for
     * the interleaved sliding window algorithm.
     * This is the window size such that all term's bases have cached precomputations of this size.
     * wNAF does not care whether it uses negative or positive powers so both are considered for each term.
     * @return The minimum window size supported by all terms.
     */
    public int computeWNafMinPrecomputedWindowSize() {
        if (terms == null) {
            return 0;
        }
        int minPrecomputedWindowSize = Integer.MAX_VALUE;
        for (MultiExpTerm term : terms) {
            minPrecomputedWindowSize = Math.min(
                    minPrecomputedWindowSize,
                    term.precomputation == null ? 0 : term.precomputation.getCurrentlySupportedWindowSize()
            );
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
