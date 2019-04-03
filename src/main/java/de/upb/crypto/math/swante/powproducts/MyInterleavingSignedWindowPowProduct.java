package de.upb.crypto.math.swante.powproducts;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.swante.MyExponentiationAlgorithms;

import java.math.BigInteger;


/**
 * Signed-window / wNAF interleaved pow product algorithm.
 */
public class MyInterleavingSignedWindowPowProduct extends MyArrayPowProductWithFixedBases {
    
    private final GroupElement[][] smallOddPowers;
    private final int windowSize;
    
    public MyInterleavingSignedWindowPowProduct(GroupElement[] bases, int windowSize) {
        super(bases);
        
        this.windowSize = windowSize;
        int m = (1 << windowSize) - 1;
        this.smallOddPowers = new GroupElement[numBases][];
        for (int i = 0; i < numBases; i++) {
            this.smallOddPowers[i] = MyExponentiationAlgorithms.precomputeSmallOddPowers(bases[i], m);
        }
    }
    
    public GroupElement evaluate(int[][] exponentDigits, int longestExponentBitLength) {
        GroupElement A = group.getNeutralElement();
        for (int j = longestExponentBitLength - 1; j >= 0; j--) {
            if (j != longestExponentBitLength - 1) {
                A = A.square();
            }
            for (int i = 0; i < numBases; i++) {
                int exponentDigit = exponentDigits[i][j];
                if (exponentDigit != 0) {
                    GroupElement power = smallOddPowers[i][Math.abs(exponentDigit) / 2];
                    if (exponentDigit < 0) {
                        power = power.inv();
                    }
                    A = A.op(power);
                }
            }
        }
        return A;
    }
    
    @Override
    public GroupElement evaluate(BigInteger[] exponents) {
        int longestExponentBitLength = getLongestExponentBitLength(exponents);
        int[][] exponentDigits = new int[numBases][];
        for (int i = 0; i < numBases; i++) {
            exponentDigits[i] = MyExponentiationAlgorithms.precomputeExponentDigitsForWNAF(exponents[i], windowSize);
        }
        return evaluate(exponentDigits, longestExponentBitLength);
    }
    
}
