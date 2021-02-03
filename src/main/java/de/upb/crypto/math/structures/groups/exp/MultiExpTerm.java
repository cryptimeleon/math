package de.upb.crypto.math.structures.groups.exp;

import de.upb.crypto.math.structures.groups.GroupElementImpl;

import java.math.BigInteger;

public class MultiExpTerm {
    protected final GroupElementImpl base;
    protected final BigInteger exponent;
    protected final SmallExponentPrecomputation precomputation;

    public MultiExpTerm(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation) {
        if (precomputation == null)
            precomputation = new SmallExponentPrecomputation(base);

        this.base = base;
        this.precomputation = precomputation;
        this.exponent = exponent;
    }

    public MultiExpTerm(GroupElementImpl base, BigInteger exponent) {
        this(base, exponent, new SmallExponentPrecomputation(base));
    }

    public GroupElementImpl getBase() {
        return base;
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public SmallExponentPrecomputation getPrecomputation() {
        return precomputation;
    }

    @Override
    public String toString() {
        return "MultiExpTerm: Base(" + base + ") Exponent(" + exponent + ") Precomputation(PosWindowSize: "
                + precomputation.getCurrentlySupportedPositiveWindowSize() + " | NegWindowSize: "
                + precomputation.getCurrentlySupportedNegativeWindowSize() + ")";
    }
}
