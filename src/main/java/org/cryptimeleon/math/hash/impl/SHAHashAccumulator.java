package org.cryptimeleon.math.hash.impl;

import org.cryptimeleon.math.hash.HashFunction;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A {@link HashAccumulator} that is used to implement the {@link HashFunction}s
 * {@link SHA256HashFunction} and {@link SHA512HashFunction}.
 *
 */
public class SHAHashAccumulator extends HashAccumulator {
    protected final MessageDigest digest;

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
