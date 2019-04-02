package de.upb.crypto.math.swante.powproducts;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.swante.MyExponentiationAlgorithms;

import java.math.BigInteger;

/**
 * Class for a pow product algorithm where windowSize many bits of each exponent
 * are looked at simultaneously. These low powers are precomputed and cached.
 * With a window size of 1, this is called "Shamir's trick".
 */
public class MySimultaneous2wAryPowProduct extends MyArrayPowProductWithFixedBases {
    
    private final GroupElement[] smallPowers;
    private final int windowSize;
    
    public MySimultaneous2wAryPowProduct(GroupElement[] bases, int windowSize) {
        super(bases);
        if (windowSize * numBases > 16) {
            throw new IllegalArgumentException("Not enough space for so many precomputations. Reduce either the windowSize or split the bases into multiple PowProducts.");
        }
        this.windowSize = windowSize;
        int numPrecomputedPowers = 1 << (windowSize * numBases);
        this.smallPowers = new GroupElement[numPrecomputedPowers];
        this.smallPowers[0] = group.getNeutralElement();
        for (int i = 1; i < (1<<windowSize); i++) {
            this.smallPowers[i] = this.smallPowers[i-1].op(bases[0]);
        }
        for (int b = 1; b < numBases; b++) {
            int shift = windowSize * b;
            for (int e = 0; e < (1 << windowSize); e++) {
                int eShifted = e << shift;
                for (int i = 0; i < (1 << shift); i++) {
                    this.smallPowers[eShifted + i] = this.smallPowers[i].op(bases[b]);
                }
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
            for (int i = windowSize-1; i >= 0; i--) {
                res = res.square();
                for (int b = 0; b < numBases; b++) {
                    e[b] &= mask;
                    e[b] <<= 1;
                    if (exponents[b].testBit(eIndex+i)) {
                        e[b]++;
                    }
                }
            }
            int eBitsConcatinated = 0;
            for (int b = 0; b < numBases; b++) {
                eBitsConcatinated <<= windowSize;
                eBitsConcatinated &= e[b];
            }
            res = res.op(smallPowers[eBitsConcatinated]);
        }
        return res;
    }
    
    
    
}
