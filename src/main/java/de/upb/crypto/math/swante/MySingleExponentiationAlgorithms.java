package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.swante.util.MyUtil;

import java.math.BigInteger;

/**
 * Class containing algorithms for computing single powers efficiently of GroupElements
 */
public class MySingleExponentiationAlgorithms {
    
    /**
     * This is the method that should be called by default for normal
     * exponentations
     * @param base
     * @param exponent
     * @return base^exponent
     */
    public static GroupElement defaultPowImplementation(GroupElement base, BigInteger exponent) {
        // Todo: switch according to most efficient expo algo (e.g. cost of invert)
        base = base.prepareForPow(exponent); // normalize base (if it is of appropriate type) so that exponentiation will be more efficient
        return simpleSquareAndMultiplyPow(base, exponent);
    }
    
    
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
            if (exponent.testBit(i)) {
                result = result.op(base);
            }
        }
        return result;
    }
    
    /**
     * @param base
     * @param exponent
     * @param windowSize
     * @param allSmallPowersOfBase: all powers from base^0 to base^m, with m=(1<<windowSize)-1, including even powers!
     * @return base^exponent using the efficient sliding window technique
     */
    public static GroupElement powUsing2wAryMethod(GroupElement base, BigInteger exponent, int windowSize, GroupElement[] allSmallPowersOfBase) {
        GroupElement res = base.getStructure().getNeutralElement();
        int l = exponent.bitLength();
        if (windowSize > 20) {
            throw new IllegalArgumentException("too large windowSize");
        }
        for (int eIndex = (l - 1) / windowSize * windowSize; eIndex >= 0; eIndex -= windowSize) {
            int e = 0;
            for (int i = windowSize - 1; i >= 0; i--) {
                res = res.square();
                e <<= 1;
                if (exponent.testBit(eIndex + i)) {
                    e++;
                }
            }
            res = res.op(allSmallPowersOfBase[e]);
        }
        return res;
    }
    
    /**
     * Precomputes the all small powers of base element. Should ideally not be called twice
     * on the same instance. You should cache the result instead.
     *
     * @param base
     * @param m integer, should not be too large
     * @return array with x^0,x^1,x^2,...,x^m
     */
    public static GroupElement[] precomputeAllSmallPowers(GroupElement base, int m) {
        GroupElement[] res = new GroupElement[m+1];
        res[0] = base.getStructure().getNeutralElement();
        for (int i = 1; i < res.length; i++) {
            res[i] = res[i-1].op(base);
        }
        return res;
    }
    
    /**
     * Precomputes the small odd powers of base element. Should ideally not be called twice
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
     * @param smallOddPowersOfBase: the result of above method when called with m=(1<<windowSize)-1
     * @return base^exponent using the efficient sliding window technique
     */
    public static GroupElement powUsingSlidingWindowMethod(GroupElement base, BigInteger exponent, int windowSize, GroupElement[] smallOddPowersOfBase) {
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
                    y = y.square();
                    if (exponent.testBit(h)) {
                        smallExponent += 1 << h - s;
                    }
                }
                
                y = y.op(smallOddPowersOfBase[smallExponent / 2]);
                i = s - 1;
            } else {
                y = y.square();
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
        if (m > 10000) {
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
        int wm = MyUtil.bitLength(m);
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
     * @param smallOddPowersOfBase odd powers up to m
     * @return base powered by the exponent given by the exponent digits
     */
    public static GroupElement powUsingLrSfwMethod(GroupElement base, int[] exponentDigits, GroupElement[] smallOddPowersOfBase) {
        int l = exponentDigits.length - 1;
        GroupElement A = smallOddPowersOfBase[Math.abs(exponentDigits[l])/2];
        if (exponentDigits[l] < 0) {
            A = A.inv();
        }
        for (int i = l-1; i >= 0; i--) {
            A = A.square();
            int exponentDigit = exponentDigits[i];
            if (exponentDigit == 0) {
                continue;
            }
            GroupElement smallPower = smallOddPowersOfBase[Math.abs(exponentDigit)/2];
            if (exponentDigit < 0) {
                smallPower = smallPower.inv();
            }
            A = A.op(smallPower);
        }
        return A;
    }
    
    /**
     *
     * @param exponent
     * @param windowSize
     * @return digits for the left-to-right (non-fractional) signed digit exponentiation algorithm
     */
    public static int[] precomputeExponentDigitsForWNAF(BigInteger exponent, int windowSize) {
        if (windowSize > 20) {
            throw new IllegalArgumentException("too large windowSize");
        }
        BigInteger c = exponent;
        int[] bi = new int[exponent.bitLength()+1];
        int i = 0;
        while (c.signum() > 0) {
            int b = 0;
            if (c.testBit(0)) {
                b = MyUtil.getNLeastSignificantBits(c.intValue(), windowSize+1);
                if (b >= 1 << windowSize) {
                    b -= 1 << (windowSize+1);
                }
                c = c.subtract(BigInteger.valueOf(b));
            }
            bi[i] = b;
            i++;
            c = c.shiftRight(1);
        }
        int[] bWithoutLeadingZeros = new int[i];
        System.arraycopy(bi, 0, bWithoutLeadingZeros, 0, i);
        return bWithoutLeadingZeros;
    }
    
    /**
     * @param base original base
     * @param exponentDigits wNAF digits of the exponent (precomputed by above method)
     * @param smallOddPowers small odd powers of the base
     * @return base^exponent, usind the wNAF approach
     */
    public static GroupElement powUsingWNafMethod(GroupElement base, int[] exponentDigits, GroupElement[] smallOddPowers) {
        GroupElement A = base.getStructure().getNeutralElement();
        for (int i = exponentDigits.length-1; i >= 0; i--) {
            if (i != exponentDigits.length-1) {
                A = A.square();
            }
            int exponentDigit = exponentDigits[i];
            if (exponentDigit != 0) {
                GroupElement power = smallOddPowers[Math.abs(exponentDigit) / 2];
                if (exponentDigit < 0) {
                    power = power.inv();
                }
                A = A.op(power);
            }
        }
        return A;
    }
    
    
}
