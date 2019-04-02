package de.upb.crypto.math.swante.powproducts;

import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;

/**
 * Class for a pow product algorithm where windowSize many bits of each exponent
 * are looked at simultaneously. These low powers are precomputed and cached.
 * With a window size of 1, this is called "Shamir's trick".
 */
public class MySimpleInterleavingPowProduct extends MyArrayPowProductWithFixedBases {
    
    private final GroupElement[][] smallPowers;
    private final int windowSize;
    
    public MySimpleInterleavingPowProduct(GroupElement[] bases, int windowSize) {
        super(bases);
        
        this.windowSize = windowSize;
        int m = (1 << windowSize) - 1;
        this.smallPowers = new GroupElement[numBases][];
        for (int i = 0; i < numBases; i++) {
            this.smallPowers[i] = new GroupElement[m+1];
            GroupElement base = bases[i];
            this.smallPowers[i][0] = base;
            for (int j = 1; j <= m; j++) {
                this.smallPowers[i][j] = this.smallPowers[i][j-1].op(base);
            }
        }
    }
    
    @Override
    public GroupElement evaluate(BigInteger[] exponents) {
        GroupElement res = group.getNeutralElement();
        int l = getLongestExponentBitLength(exponents);
        int[] e = new int[numBases];
        int mask = (1 << windowSize) - 1;
        for (int eIndex = (l-1)/windowSize*windowSize; eIndex >= 0; eIndex -= windowSize) {
            for (int i = windowSize-1; i >= 0; i++) {
                res = res.square();
                for (int b = 0; b < numBases; b++) {
                    e[b] &= mask;
                    e[b] <<= 1;
                    if (exponents[b].testBit(eIndex+i)) {
                        e[b]++;
                    }
                }
            }
            for (int b = 0; b < numBases; b++) {
                if (exponents[b].testBit(eIndex)) {
                    res = res.op(bases[b]);
                }
            }
        }
        return res;
    }
    
    
    
}
