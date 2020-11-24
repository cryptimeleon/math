package de.upb.crypto.math.test;

import de.upb.crypto.math.structures.groups.exp.ExponentiationAlgorithms;

import java.math.BigInteger;
import java.util.Arrays;

public class ManualTest {
    public static void main(String[] args) {
        BigInteger exponent = BigInteger.valueOf(-58);
        int[] exponentDigits = ExponentiationAlgorithms.precomputeExponentDigitsForWnaf(exponent, 4);
        System.out.println(Arrays.toString(exponentDigits));
    }
}
