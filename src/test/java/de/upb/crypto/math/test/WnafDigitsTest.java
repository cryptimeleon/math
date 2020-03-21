package de.upb.crypto.math.test;

import de.upb.crypto.math.interfaces.structures.UncachedGroupPrecomputations;

import java.math.BigInteger;

public class WnafDigitsTest {

    public static void main(String[] args) {
        int[] testNumbers = new int[100000];
        BigInteger[] testBigNumbers = new BigInteger[testNumbers.length];
        for (int i = 0; i < testNumbers.length; ++i) {
            testNumbers[i] = Integer.MAX_VALUE / 2 + i;
            testBigNumbers[i] = BigInteger.valueOf(testNumbers[i]);
        }

        int numRuns = 40;
        int warmupRuns = 5;
        long startTime;
        int windowSize = 8;
        /*
        long[] bigIntTimes = new long[numRuns];
        long[] optTimes = new long[numRuns];
        long[] intTimes = new long[numRuns];

        for (int i = 0; i < numRuns + warmupRuns; ++i) {
            startTime = System.currentTimeMillis();
            for (BigInteger n : testBigNumbers) {
                UncachedGroupPrecomputations.precomputeExponentDigitsForWnaf(n, windowSize);
            }
            if (i >= warmupRuns) {
                bigIntTimes[i-warmupRuns] = System.currentTimeMillis() - startTime;
            }

            startTime = System.currentTimeMillis();
            for (BigInteger n : testBigNumbers) {
                UncachedGroupPrecomputations.precomputeExponentDigitsForWnafOpt(n, windowSize);
            }
            if (i >= warmupRuns) {
                optTimes[i-warmupRuns] = System.currentTimeMillis() - startTime;
            }

            startTime = System.currentTimeMillis();
            for (int n : testNumbers) {
                UncachedGroupPrecomputations.precomputeExponentDigitsForWnafInt(n, windowSize);
            }
            if (i >= warmupRuns) {
                intTimes[i-warmupRuns] = System.currentTimeMillis() - startTime;
            }
        }

        System.out.println("----- Results -----");
        System.out.println("BigInteger average: " + average(bigIntTimes));
        System.out.println("BigInteger minimum: " + minimum(bigIntTimes));
        System.out.println("Opt average: " + average(optTimes));
        System.out.println("Opt minimum: " + minimum(optTimes));
        System.out.println("int average: " + average(intTimes));
        System.out.println("int minimum: " + minimum(intTimes));*/
    }

    public static double average(long[] dataPoints) {
        double avg = 0;
        for (long dataPoint : dataPoints) {
            avg += dataPoint;
        }
        return avg / dataPoints.length;
    }

    public static long minimum(long[] dataPoints) {
        long min = dataPoints[0];
        for (long dataPoint : dataPoints) {
            if (dataPoint < min)
                min = dataPoint;
        }
        return min;
    }
}