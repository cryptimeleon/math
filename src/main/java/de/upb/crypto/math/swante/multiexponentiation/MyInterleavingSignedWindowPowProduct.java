package de.upb.crypto.math.swante.multiexponentiation;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.swante.MySingleExponentiationAlgorithms;

import java.math.BigInteger;


/**
 * Signed-window / wNAF interleaved pow product algorithm.
 * See paper "Algorithms for multi-exponentiation" by Bodo Moeller
 */
public class MyInterleavingSignedWindowPowProduct extends MyBasicPowProduct {
    
    private final GroupElement[][] smallOddPowers;
    private final int windowSize;
    
    public MyInterleavingSignedWindowPowProduct(GroupElement[] bases, int windowSize) {
        super(bases);
        
        this.windowSize = windowSize;
        int m = (1 << windowSize) - 1;
        this.smallOddPowers = new GroupElement[numBases][];
        for (int i = 0; i < numBases; i++) {
            this.smallOddPowers[i] = MySingleExponentiationAlgorithms.precomputeSmallOddPowers(bases[i], m);
        }
    }
    
    /**
     *
     * @param exponentDigits array of the wNAF exponent digits (should all have the same length)
     * @param longestExponentDigitLength number of digits of the largest exponent
     * @return
     */
    public GroupElement evaluate(int[][] exponentDigits, int longestExponentDigitLength) {
        GroupElement A = group.getNeutralElement();
        for (int j = longestExponentDigitLength - 1; j >= 0; j--) {
            if (j != longestExponentDigitLength - 1) {
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
        int longestExponentDigitLength = 0;
        int[][] exponentDigits = new int[numBases][];
        for (int i = 0; i < numBases; i++) {
            exponentDigits[i] = MySingleExponentiationAlgorithms.precomputeExponentDigitsForWNAF(exponents[i], windowSize);
            longestExponentDigitLength = Math.max(longestExponentDigitLength, exponentDigits[i].length);
        }
        // padding with zeros:
        for (int i = 0; i < numBases; i++) {
            int[] paddedArray = new int[longestExponentDigitLength];
            System.arraycopy(exponentDigits[i], 0, paddedArray, 0, exponentDigits[i].length);
            exponentDigits[i] = paddedArray;
        }
        return evaluate(exponentDigits, longestExponentDigitLength);
    }
    
}
