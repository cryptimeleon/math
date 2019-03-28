package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;

/**
 * Class containing algorithms for computing powers efficiently of GroupElements
 */
public class MyExponentiationAlgorithms {
    
    /**
     * Very basic square & multiply exponentiation
     * @param base
     * @param exponent
     * @return
     */
    public static GroupElement simpleSquareAndMultiplyPow(GroupElement base, BigInteger exponent) {
        if (exponent.signum() < 0)
            return simpleSquareAndMultiplyPow(base, exponent.negate()).inv();
    
        GroupElement result = base.getStructure().getNeutralElement();
        for (int i = exponent.bitLength() - 1; i >= 0; i--) {
            result = result.square();
            if (exponent.testBit(i))
                result = result.op(base);
        }
        return result;
    }
    
    /**
     * Precomputes the small powers of base element. Should ideally not be called twice
     * on the same instance. You should cache the result instead.
     *
     * @param base
     * @param m odd integer, should not be too large
     * @return array with x^1,x^3,x^5,...,x^m
     */
    public static GroupElement[] precomputeSmallOddPowers(GroupElement base, int m) {
        GroupElement[] res = new GroupElement[(m+1)/2];
        GroupElement xx = base.square();
        res[0] = base;
        for (int i = 1; i < res.length; i++) {
            res[i] = res[i-1].op(xx);
        }
        return res;
    }
    
    /**
     * @param base
     * @param exponent
     * @param windowSize
     * @param smallPowersOfBase: the result of above method when called with m=(1<<windowSize)-1
     * @return base^exponent using the efficient sliding window technique
     */
    public static GroupElement powUsingSlidingWindow(GroupElement base, BigInteger exponent, int windowSize, GroupElement[] smallPowersOfBase) {
        GroupElement y = base.getStructure().getNeutralElement();
        int l = exponent.bitLength();
        int i = l - 1;
        if (windowSize > 20) {
            throw new IllegalArgumentException("too large windowSize");
        }
        while (i > -1) {
            if (exponent.testBit(i)) {
                int s = Math.max(0, i - windowSize + 1);
                int smallExponent = 0;
                while (!exponent.testBit(s)) {
                    s++;
                }
                for (int h = s; h <= i; h++) {
                    y = y.op(y);
                    if (exponent.testBit(h)) {
                        smallExponent += 1 << h - s;
                    }
                }
                
                y = y.op(smallPowersOfBase[smallExponent / 2]);
                i = s - 1;
            } else {
                y = y.op(y);
                i--;
            }
        }
        return y;
    }
    
    public static int[] precomputeExponentTransformationForLrSfwMethod(BigInteger exponent, int m) {
        if (m > 1000) {
            throw new IllegalArgumentException("too large m");
        }
    }

    public static GroupElement powUsingLrSfwMethod(GroupElement base, int[] exponentDigits) {
        GroupElement A = base.square();
        
    }

}
