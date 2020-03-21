package de.upb.crypto.math.test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

public class ManualTest {
    public static void main(String[] args) {
        for (int n = -100; n < 1000; ++n) {
            System.out.println(n);
            //System.out.println("Bit string: " + Integer.toBinaryString(n));
            //System.out.println("Correct result: " + (n >> 1));
            BigInteger val = BigInteger.valueOf(n);

            // signed right bit shift
            final byte maskOfCarry = 0x01;
            byte[] aBytes = val.toByteArray();
            //System.out.println("Before: " + Arrays.toString(aBytes));

            boolean carry = ((aBytes[0] & maskOfCarry) != 0);
            aBytes[0] >>= 1;
            for (int i = 1; i < aBytes.length; ++i) {
                if (carry) {
                    carry = ((aBytes[i] & maskOfCarry) != 0);
                    // java casts byte to int before shift so this is necessary
                    aBytes[i] = (byte) ((aBytes[i] & 0xff) >>> 1);
                    aBytes[i] += 0x80;
                } else {
                    carry = ((aBytes[i] & maskOfCarry) != 0);
                    aBytes[i] = (byte) ((aBytes[i] & 0xff) >>> 1);
                }
                //System.out.println("After: " + Arrays.toString(aBytes));
            }
            //System.out.println("After: " + Arrays.toString(aBytes));
            //System.out.println("Result: " + new BigInteger(aBytes));
            assert(new BigInteger(aBytes).equals(val));
        }

    }
}
