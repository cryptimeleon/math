package de.upb.crypto.math.swante.powproducts;

import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;
import java.util.stream.IntStream;

public class MySimultaneousSlidingWindowPowProduct extends MyArrayPowProductWithFixedBases {
    
    private final GroupElement[] smallPowers;
    private final int windowSize;
    
    public MySimultaneousSlidingWindowPowProduct(GroupElement[] bases, int windowSize) {
        super(bases);
        if (windowSize * numBases > 24) {
            throw new IllegalArgumentException("Not enough space for so many precomputations. Reduce either the windowSize or split the bases into multiple PowProducts.");
        }
        this.windowSize = windowSize;
        this.smallPowers = computeAllSmallPowerProducts(windowSize);
    }
    
    @Override
    public GroupElement evaluate(BigInteger[] exponents) {
        GroupElement A = group.getNeutralElement();
        int j = getLongestExponentBitLength(exponents) - 1;
        while (j >= 0) {
            final int finalj = j;
            if (IntStream.range(0, numBases).noneMatch(it -> exponents[it].testBit(finalj))) {
                A = A.square();
                j--;
            } else {
                int jNew = Math.max(j - windowSize, -1);
                int J = jNew + 1;
                
                while (true) {
                    final int finalJ = J;
                    if (IntStream.range(0, numBases).anyMatch(it -> exponents[it].testBit(finalJ))) {
                        break;
                    }
                    J++;
                }
                int e = 0;
                for (int i = numBases - 1; i >= 0; i--) {
                    int ePart = 0;
                    for (int k = j; k >= J; k--) {
                        ePart <<= 1;
                        if (exponents[i].testBit(k)) {
                            ePart++;
                        }
                    }
                    e <<= windowSize;
                    e |= ePart;
                }
                while (j >= J) {
                    A = A.square();
                    j--;
                }
                A = A.op(smallPowers[e]);
                while (j > jNew) {
                    A = A.square();
                    j--;
                }
            }
        }
        return A;
    }
    
    
}
