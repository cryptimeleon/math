package de.upb.crypto.math.swante.powproducts;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.swante.MyExponentiationAlgorithms;

import java.math.BigInteger;

/**
 * Class for a pow product algorithm where windowSize many bits of each exponent
 * are looked at simultaneously. These low powers are precomputed and cached.
 * With a window size of 1, this is called "Shamir's trick".
 */
public class MySimpleInterleavingPowProduct extends MyArrayPowProductWithFixedBases {
    
    private final GroupElement[][] smallOddPowers;
    private final int windowSize;
    
    public MySimpleInterleavingPowProduct(GroupElement[] bases, int windowSize) {
        super(bases);
        
        this.windowSize = windowSize;
        int m = (1 << windowSize) - 1;
        this.smallOddPowers = new GroupElement[numBases][];
        for (int i = 0; i < numBases; i++) {
            this.smallOddPowers[i] = MyExponentiationAlgorithms.precomputeSmallOddPowers(bases[i], m);
        }
    }
    
    @Override
    public GroupElement evaluate(BigInteger[] exponents) {
        GroupElement A = group.getNeutralElement();
        int longestExponentBitLength = getLongestExponentBitLength(exponents);
        int[] wh = new int[numBases];
        int[] e = new int[numBases];
        for (int i = 0; i < numBases; i++) {
            wh[i] = -1;
        }
        for (int j = longestExponentBitLength - 1; j >= 0; j--) {
            if (j != longestExponentBitLength - 1) {
                A = A.square();
            }
            for (int i = 0; i < numBases; i++) {
                if (wh[i] == -1 && exponents[i].testBit(j)) {
                    int J = j - windowSize + 1;
                    while (!testBit(exponents[i], J)) {
                        J++;
                    }
                    wh[i] = J;
                    e[i] = 0;
                    for (int k = J; k <= j; k++) {
                        e[i] <<= 1;
                        if (testBit(exponents[i], k)) {
                            e[i]++;
                        }
                    }
                }
                if (wh[i] == j) {
                    A = A.op(smallOddPowers[i][e[i] / 2]);
                    wh[i] = -1;
                }
            }
        }
        return A;
    }
    
}
