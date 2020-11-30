package de.upb.crypto.math.hash.impl;

import de.upb.crypto.math.interfaces.hash.HashFunction;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

/**
 * The base class for SHA hash functions which implements some common methods.
 *
 * @author Mirko Jürgens, refactoring: Denis Diemert
 */
abstract class AbstractSHAHashFunction implements HashFunction, StandaloneRepresentable {

    private final String algorithm;

    /**
     * Initializes this hash function with a specific SHA algorithm.
     * @param algorithm the name of the SHA algorithm.
     */
    AbstractSHAHashFunction(final String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Applies this hash function to the given {@link UniqueByteRepresentable}.
     *
     * @param ubr the {@code UniqueByteRepresentable} to hash.
     * @return the output of the hash function in form of a {@code byte[]}
     */
    @Override
    public byte[] hash(final UniqueByteRepresentable ubr) {
        final SHAHashAccumulator accu = new SHAHashAccumulator(algorithm);
        accu.append(ubr.getUniqueByteRepresentation());
        return accu.extractBytes();
    }

    @Override
    public Representation getRepresentation() {
        return null;
    }

    /**
     * Applies this hash function to the given {@code byte[]}.
     *
     * @param bytes the {@code byte[]} to hash.
     * @return the output of the hash function in form of a {@code byte[]}
     */
    @Override
    public byte[] hash(final byte[] bytes) {
        final SHAHashAccumulator accu = new SHAHashAccumulator(algorithm);
        accu.append(bytes);
        return accu.extractBytes();
    }
}
