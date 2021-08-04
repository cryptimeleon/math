package org.cryptimeleon.math;

import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

public class ManualTest {
    public static void main(String args[]) {
        BigInteger n = BigInteger.valueOf((long) Math.pow(2, 24)); // 2^25
        System.out.println(n.bitLength() - 1);
        Zn zn = new Zn(n);
        System.out.println("001");
        System.out.println(zn.injectiveValueOf(new byte[] {0, 0, 1}));
        System.out.println("01");
        System.out.println(zn.injectiveValueOf(new byte[] {0, 1}));
    }
}
