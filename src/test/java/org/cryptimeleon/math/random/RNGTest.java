package org.cryptimeleon.math.random;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

public class RNGTest {


    @Test
    public void testBigInteger() {
        RandomGeneratorImpl rng = new SecureRandomGeneratorImpl();
        System.out.println("Test RNG for integers.");

        for (int size = 1; size <= 16; size++) {
            System.out.println("size:" + size);
            for (int offset = -1; offset <= 1; offset++) {
                BigInteger range = BigInteger.ONE.shiftLeft(size).add(BigInteger.valueOf(offset));

                for (int i = 0; i < 100; i++) {
                    BigInteger r = rng.getRandomNumber(range);
                    Assert.assertTrue(range.compareTo(r) > 0);
                    Assert.assertTrue(r.compareTo(BigInteger.ZERO) >= 0);
                }
            }
        }
    }

    @Test
    public void testDistribution() {
        RandomGeneratorImpl rng = new SecureRandomGeneratorImpl();

        System.out.println("Test distribution of generated numbers");
        int[] ranges = {255, 256, 257};
        int k = 100000;

        for (int n : ranges) {
            int[] buckets = new int[n];

            for (int i = 0; i < k; i++) {
                buckets[rng.getRandomNumber(BigInteger.valueOf(n)).intValue()]++;
            }

            /*check that each bucket obtains nearly the same number of elements*/
            Arrays.sort(buckets);
            Assert.assertTrue(buckets[0] > k / n - 200);
            Assert.assertTrue(buckets[buckets.length - 1] < k / n + 200);
        }
    }
}
