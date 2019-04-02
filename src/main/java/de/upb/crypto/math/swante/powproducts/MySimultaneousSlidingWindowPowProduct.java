package de.upb.crypto.math.swante.powproducts;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.swante.MyExponentiationAlgorithms;

import java.math.BigInteger;

/**
 * Class for a pow product algorithm where windowSize many bits of each exponent
 * are looked at simultaneously. These low powers are precomputed and cached
 */
public class MySimultaneousSlidingWindowPowProduct extends MyArrayPowProductWithFixedBases {
    
    private final GroupElement[][] smallPowers;
    
    public MySimultaneousSlidingWindowPowProduct(GroupElement[] bases, int windowSize) {
        super(bases);
        if (windowSize * numBases > 16) {
            throw new IllegalArgumentException("Not enough space for so many precomputations. Reduce either the windowSize or split the bases into multiple PowProducts.");
        }
        int m = (1 << windowSize) - 1;
        this.smallPowers = new GroupElement[numBases][];
        for (int i = 0; i < numBases; i++) {
            this.smallPowers[i] = MyExponentiationAlgorithms.precomputeSmallOddPowers(bases[i], m);
        }
    }
    
    @Override
    public GroupElement evaluate(BigInteger[] exponents) {
        GroupElement res = group.getNeutralElement();
        int longestExponentBitLength = getLongestExponentBitLength(exponents);
        for (int e = longestExponentBitLength-1; e >= 0; e--) {
            if (e != longestExponentBitLength-1) {
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
