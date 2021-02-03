package de.upb.crypto.math.random;

import java.math.BigInteger;

public interface RandomGeneratorImpl {
    /**
     * Generates a uniformly random number between {@code 0} (inclusive) and {@code exclusiveUpperBound} (exclusive).
     */
    default BigInteger getRandomNumber(BigInteger exclusiveUpperBound) {
        int n = exclusiveUpperBound.subtract(BigInteger.ONE).bitLength();

        /*account for sign bit. BigIntegers are constructed with sign bit*/
        n++;

        int byte_length = n / 8;
        //round up to byte boundary
        if (0 != n % 8) {
            byte_length++;
        }

        /*zero out BigEndian  MSBs including reserved sign bit to get range 0...2^n-1*/
        byte mask = (byte) 0x7f;
        if (0 != n % 8) {
            mask = (byte) (mask >> (8 - (n % 8)));
        }

        BigInteger result;
        do {
            byte[] random_bytes = getRandomBytes(byte_length);

            /* by always masking out sign bit, we get a positive number*/
            /*furthermore, by masking out MSB of Big Endian representation, we get an integer
             * in the range 0...2^(n-1)-1 */

            random_bytes[0] = (byte) (random_bytes[0] & mask);

            /*create big integer */
            result = new BigInteger(random_bytes);

            /*now check if we are smaller than l or in the range l..2^(n-1)-1*/
            /*discard the latter*/
        } while (result.compareTo(exclusiveUpperBound) >= 0);

        return result;
    }

    /**
     * Generates a uniformly random number between {@code inclusiveLowerBound} (inclusive) and {@code exclusiveUpperBound} (exclusive).
     */
    default BigInteger getRandomNumber(BigInteger inclusiveLowerBound, BigInteger exclusiveUpperBound) {
        return getRandomNumber(exclusiveUpperBound.subtract(inclusiveLowerBound)).add(inclusiveLowerBound);
    }

    /**
     * Generates a uniformly random number between \(0\) and \(2^{\text{bitlength}}-1\) (inclusive).
     */
    default BigInteger getRandomNumberOfBitlength(int bitlength) {
        return getRandomNumber(BigInteger.ONE.shiftLeft(bitlength));
    }

    /**
     * Returns random byte array of given length.
     *
     * @param l length of resulting byte array
     */
    byte[] getRandomBytes(int l);

    default boolean getRandomBit() {
        return getRandomBytes(1)[0] % 2 == 0;
    };

    /**
     * Generates a random prime number from the interval \([2^{n-1}, 2^n-1]\).
     *
     * @param bitlength desired number of bits for the prime number
     * @return a {@code BigInteger} that is probably prime
     */
    default BigInteger getRandomPrime(int bitlength) {
        BigInteger lowerBound = BigInteger.ONE.shiftLeft(bitlength - 1); // 2^(n-1)
        BigInteger upperBound = BigInteger.ONE.shiftLeft(bitlength).subtract(BigInteger.ONE);

        BigInteger candidate;
        do {
            candidate = getRandomNumber(lowerBound, upperBound);
        } while (!candidate.isProbablePrime(128));

        return candidate;
    }
}
