package de.upb.crypto.math.random.SimpleRandomGenerator;

import de.upb.crypto.math.random.interfaces.RandomGenerator;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SimpleRandomGenerator implements RandomGenerator {

    BigInteger seed;
    SecureRandom rng;

    public SimpleRandomGenerator() {
        rng = new SecureRandom();
    }


    @Override
    public void setSeed(BigInteger seed) {
        this.seed = seed;
        rng.setSeed(seed.longValue());
    }

    @Override
    public boolean nextBit() {
        return rng.nextBoolean();
    }

    @Override
    public byte[] getRandomByteArray(int l) {
        byte[] b = new byte[l];
        rng.nextBytes(b);
        return b;
    }

	/*public BigInteger getRandomElement(BigInteger l) { //migrated to RandomGenerator interface as default method
		int n = l.bitLength();
		l = l.subtract(BigInteger.ONE);
		
		BigInteger result = BigInteger.ZERO;
		do {
			for (int i = 0; i < n; i++) {
				if (rng.nextBoolean()) {
					result = result.setBit(i);
				}else result = result.clearBit(i);
			}
		} while (result.bitLength() == 0 || result.compareTo(l) == 1);
		return result;
	}*/
}