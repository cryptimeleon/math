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
     * to reach the desired window size.
     * If {@code computeNegativePowers} is set to {@code true}, terms with negative exponents will have the
     * precomputation done for negative powers.
     * @param windowSize The window size to ensure support for
     * @param computeNegativePowers Whether to precompute negative powers when exponent is negative
     */
    public void ensurePrecomputation(int windowSize, boolean computeNegativePowers) {
        if (terms != null && windowSize > computeMinPrecomputedWindowSize(computeNegativePowers)) {
            for (MultiExpTerm term : terms) {
                if (!computeNegativePowers || term.getExponent().signum() >= 0) {
                    term.getPrecomputation().compute(windowSize);
                } else {
                    term.getPrecomputation().computeNegativePowers(windowSize);
                }
            }
        }
    }

    public List<MultiExpTerm> getTerms() {
        return terms == null ? Collections.emptyList() : Collections.unmodifiableList(terms);
    }

    public int getNumberOfTerms() {
        return terms == null ? 0 : terms.size();
    }

    /**
     * Computes the minimum window size currently offered by the precomputations of all terms.
     * This is the window size such that all term's bases have cached precomputations of this size.
     * When {@code considerNegativePowers} is set to {@code true}, terms with negative exponents will have their
     * negative power precomputation window size used for this computation.
     * @param considerNegativePowers Whether to consider the negative window size in the calculation
     * @return The minimum window size supported by all terms.
     */
    public int computeMinPrecomputedWindowSize(boolean considerNegativePowers) {
        if (terms == null) {
            return 0;
        }
        int minPrecomputedWindowSize = Integer.MAX_VALUE;
        for (MultiExpTerm term : terms) {
            if (!considerNegativePowers || term.getExponent().signum() >= 0) {
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
