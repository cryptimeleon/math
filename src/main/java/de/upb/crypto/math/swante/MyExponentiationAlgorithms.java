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
    
    /**
     * Algorithm based on the following paper:
     * Möller, Bodo. "Fractional windows revisited: Improved signed-digit representations for efficient exponentiation." International Conference on Information Security and Cryptology. Springer, Berlin, Heidelberg, 2004.
     * @param exponent
     * @param m odd positive integer
     * @return int array for the left-to-right signed-fractional-window exponent, each element being zero or odd and in [-m,...,m]
     */
    public static int[] precomputeExponentTransformationForLrSfwMethod(BigInteger exponent, int m) {
        if (m > 1000) {
            throw new IllegalArgumentException("too large m");
        }
        int lambda =  exponent.bitLength()-1;
        byte[] beta = new byte[lambda + 2];
        int[] b = new int[lambda + 2];
        for (int i = 0; i <= lambda; i++) {
            if (exponent.testBit(i)) {
                beta[i] = 1;
            }
        }
        int i = lambda +1;
        int l = 0;
        int wm = 1+(int)(Math.log(m)/Math.log(2.0));
        while (i >= 0) {
            if (beta[i] == (i==0 ? 0 : beta[i-1])) {
                i--;
            }  else {
                int W = wm+1;
                int d = -beta[i];
                for (int j = i-1; j >= i-W+1; j--) {
                    d *= 2;
                    d += j < 0 ? 0 : beta[j];
                }
                d += (i-W) < 0 ? 0 : beta[i-W];
                if (d %2==1 && Math.abs(d) > m) {
                    if (i-W >= 0) {
                        d -= beta[i-W];
                    }
                    if (i-W+1 >= 0) {
                        d -= beta[i - W + 1];
                    }
                    d /= 2;
                    W = wm;
                    d += (i-W) < 0 ? 0 : beta[i-W];
                }
                int next_i = i - W;
                i = next_i + 1;
                while (d %2==0) {
                    i++;
                    d /= 2;
                }
                b[i] = d;
                if (i > l) {
                    l = i;
                }
                i = next_i;
            }
        }
        int[] bWithoutLeadingZeros = new int[l+1];
        System.arraycopy(b, 0, bWithoutLeadingZeros, 0, l + 1);
        return bWithoutLeadingZeros;
    }
    
    /**
     * Efficient exponentiation algorithm that should be used when inversion is cheap.
     * It uses the Left-2-Right signed digit transformation of the exponent, in order
     * to reduce the number of non-zero digits in the exponent.
     * @param base
     * @param exponentDigits odd signed digits of exponent with abs value <= m, can also be equal to zero
     * @param smallPowersOfBase odd powers up to m
     * @return
     */
    public static GroupElement powUsingLrSfwMethod(GroupElement base, int[] exponentDigits, GroupElement[] smallPowersOfBase) {
        int l = exponentDigits.length - 1;
        GroupElement A = smallPowersOfBase[Math.abs(exponentDigits[l])/2];
        if (exponentDigits[l] < 0) {
            A = A.inv();
        }
        for (int i = l-1; i >= 0; i--) {
            A = A.square();
            int exponentDigit = exponentDigits[i];
            GroupElement smallPower = smallPowersOfBase[Math.abs(exponentDigit)/2];
            if (exponentDigit < 0) {
                smallPower = smallPower.inv();
            }
            A = A.op(smallPower);
        }
        return A;
    }

}
