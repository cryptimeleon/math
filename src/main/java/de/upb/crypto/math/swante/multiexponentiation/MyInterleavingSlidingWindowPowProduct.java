package de.upb.crypto.math.swante.multiexponentiation;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.swante.MySingleExponentiationAlgorithms;

import java.math.BigInteger;

/**
 * Class for computing a multiexponentiation using the interleaving sliding window
 * technique.
 * See paper "Algorithms for multi-exponentiation" by Bodo Moeller
 */
public class MyInterleavingSlidingWindowPowProduct extends MyBasicPowProduct {
    
    private final GroupElement[][] smallOddPowers;
    private final int windowSize;
    
    public MyInterleavingSlidingWindowPowProduct(GroupElement[] bases, int windowSize) {
        super(bases);
        
        this.windowSize = windowSize;
        int m = (1 << windowSize) - 1;
        this.smallOddPowers = new GroupElement[numBases][];
        for (int i = 0; i < numBases; i++) {
            this.smallOddPowers[i] = MySingleExponentiationAlgorithms.precomputeSmallOddPowers(bases[i], m);
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
                    for (int k = j; k >= J; k--) {
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
