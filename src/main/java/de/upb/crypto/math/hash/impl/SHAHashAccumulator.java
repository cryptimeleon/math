package de.upb.crypto.math.hash.impl;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.hash.HashFunction;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A {@link HashAccumulator} that is used to implement the {@link HashFunction}s
 * {@link SHA256HashFunction} and {@link SHA512HashFunction}.
 *
 * @author Denis Diemert
 */
public class SHAHashAccumulator extends HashAccumulator {
    private final MessageDigest digest;

    public SHAHashAccumulator(final String algorithm) {
        try {
            this.digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void append(final byte[] bytes) {
        digest.update(bytes);
    }

    @Override
    public byte[] extractBytes() {
        return digest.digest();
    }
}
