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
    int minPrecomputedWindowSize = Integer.MAX_VALUE;
    GroupElementImpl constantFactor = null;

    public void put(MultiExpTerm term) {
        if (terms == null)
            terms = new ArrayList<>();

        terms.add(term);
        minPrecomputedWindowSize = Math.min(minPrecomputedWindowSize, term.precomputation == null ? 0 : term.precomputation.getCurrentSupportedWindowSize());
    }

    public void put(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation) {
        put(new MultiExpTerm(base, exponent, precomputation));
    }

    public void put(GroupElementImpl groupelem) {
        constantFactor = constantFactor == null ? groupelem : constantFactor.op(groupelem);
    }

    public void ensurePrecomputation(int windowSize) {
        if (terms != null && windowSize > minPrecomputedWindowSize) {
            terms.forEach(t -> t.getPrecomputation().compute(windowSize));
            minPrecomputedWindowSize = windowSize;
        }
    }

    public List<MultiExpTerm> getTerms() {
        return terms == null ? Collections.emptyList() : Collections.unmodifiableList(terms);
    }

    public int getNumberOfTerms() {
        return terms == null ? 0 : terms.size();
    }

    public int getMinPrecomputedWindowSize() {
        return terms == null ? 0 : minPrecomputedWindowSize;
    }

    public Optional<GroupElementImpl> getConstantFactor() {
        return Optional.ofNullable(constantFactor);
    }

    public boolean isEmpty() {
        return (terms == null || terms.size() == 0) && constantFactor == null;
    }
}
