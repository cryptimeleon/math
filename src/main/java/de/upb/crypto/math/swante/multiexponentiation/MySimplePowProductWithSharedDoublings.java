package de.upb.crypto.math.swante.multiexponentiation;

import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;

public class MySimplePowProductWithSharedDoublings extends MyBasicPowProduct {
    
    public MySimplePowProductWithSharedDoublings(GroupElement[] bases) {
        super(bases);
    }
    
    /**
     * A rather simple implementation without any caching,
     * but at least processing the bases in parallel.
     * E.g. a^3*b^2 -> (a*b)^2*a
     */
    @Override
    public GroupElement evaluate(BigInteger[] exponents) {
        GroupElement res = group.getNeutralElement();
        int longestExponentBitLength = getLongestExponentBitLength(exponents);
        for (int e = longestExponentBitLength - 1; e >= 0; e--) {
            if (e != longestExponentBitLength - 1) {
                res = res.square();
            }
            for (int b = 0; b < numBases; b++) {
                if (exponents[b].testBit(e)) {
                    res = res.op(bases[b]);
                }
            }
        }
        return res;
    }
    
}
