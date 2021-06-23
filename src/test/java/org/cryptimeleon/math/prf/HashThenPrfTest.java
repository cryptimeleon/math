package org.cryptimeleon.math.prf;

import org.cryptimeleon.math.hash.HashFunction;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.hash.impl.SHA256HashFunction;
import org.cryptimeleon.math.hash.impl.SHA512HashFunction;
import org.cryptimeleon.math.misc.ByteArrayImpl;
import org.cryptimeleon.math.prf.zn.HashThenPrfToZn;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Tests the hash-then-prf-to-Zn construction.
 */
@RunWith(Parameterized.class)
public class HashThenPrfTest {
    private HashThenPrfToZn hashThenPrfToZn;
    private Supplier<UniqueByteRepresentable> hashPreimageSupplier;


    public HashThenPrfTest(TestParams params) {
        hashThenPrfToZn = params.hashThenPrfToZn;
        hashPreimageSupplier = params.hashPreimageSupplier;
    }

    @org.junit.Test
    public void testRun() {
        PrfKey k = hashThenPrfToZn.generateKey();
        assertNotNull(hashThenPrfToZn.hashThenPrfToZn(k, hashPreimageSupplier.get()));
    }

    @org.junit.Test
    public void testVector() {
        PrfKey k = hashThenPrfToZn.generateKey();
        UniqueByteRepresentable preimage = hashPreimageSupplier.get();

        assertNotNull(hashThenPrfToZn.hashThenPrfToZnVector(k, preimage, 13));
        assertNotEquals(
                hashThenPrfToZn.hashThenPrfToZnVector(k, preimage, 12, "preimage1"),
                hashThenPrfToZn.hashThenPrfToZnVector(k, preimage, 12, "preimage2")
        );
        assertNotEquals(
                hashThenPrfToZn.hashThenPrfToZnVector(k, preimage, 12, "preimage2"),
                hashThenPrfToZn.hashThenPrfToZnVector(k, preimage, 12, "")
        );
        assertNotEquals(
                hashThenPrfToZn.hashThenPrfToZnVector(k, preimage, 7),
                hashThenPrfToZn.hashThenPrfToZnVector(k, preimage, 12).truncate(7)
        ); // Different size vectors should have different elements
        assertEquals(
                hashThenPrfToZn.hashThenPrfToZnVector(k, preimage, 7, "samePreimage"),
                hashThenPrfToZn.hashThenPrfToZnVector(k, preimage, 7, "samePreimage")
        );
    }

    // Some test configurations
    @Parameterized.Parameters(name = "Test: {0}") // add (name="Test: {0}") for jUnit 4.12+ to print ring's name to test
    public static Collection<TestParams[]> data() {
        return Arrays.asList(new TestParams[][]{
                {new TestParams(128, new SHA256HashFunction(), 30, () -> ByteArrayImpl.fromRandom(1024))},
                {new TestParams(128, new SHA256HashFunction(), 470, () -> ByteArrayImpl.fromRandom(1024))},
                {new TestParams(128, new SHA256HashFunction(), 1330, () -> ByteArrayImpl.fromRandom(1024))},
                {new TestParams(128, new SHA512HashFunction(), 470, () -> ByteArrayImpl.fromRandom(1024))},
                {new TestParams(256, new SHA256HashFunction(), 30, () -> ByteArrayImpl.fromRandom(1024))},
                {new TestParams(256, new SHA256HashFunction(), 470, () -> ByteArrayImpl.fromRandom(1024))},
                {new TestParams(256, new SHA256HashFunction(), 1330, () -> ByteArrayImpl.fromRandom(1024))},
                {new TestParams(256, new SHA512HashFunction(), 470, () -> ByteArrayImpl.fromRandom(1024))},
        });
    }

    /**
     * Simple data class for test parameters.
     */
    private static class TestParams {
        private int aesKeyLength;
        private HashFunction hashFunction;
        private Zn zn;
        private Supplier<UniqueByteRepresentable> hashPreimageSupplier;
        private HashThenPrfToZn hashThenPrfToZn;
        private int znBitLength;

        public TestParams(int aesKeyLength, HashFunction hashFunction, int znBitLength, Supplier<UniqueByteRepresentable> hashPreimageSupplier) {
            this.aesKeyLength = aesKeyLength;
            this.hashFunction = hashFunction;
            this.hashPreimageSupplier = hashPreimageSupplier;
            this.zn = new Zn(new BigInteger(znBitLength, 64, new Random()));
            this.hashThenPrfToZn = new HashThenPrfToZn(aesKeyLength, zn, hashFunction, 128);
            this.znBitLength = znBitLength;
        }

        @Override
        public String toString() {
            return aesKeyLength + "bit AES with " + hashFunction.getClass().getName() + ", Zn of size " + znBitLength;
        }
    }
}
