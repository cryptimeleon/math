package de.upb.crypto.math.hash.impl;

import de.upb.crypto.math.interfaces.hash.HashFunction;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

/**
 * Maps a {@link SHAHashAccumulator} to the {@link HashFunction} interface.
 *
 * @author Mirko Jürgens, refactoring: Denis Diemert
 */
abstract class AbstractSHAHashFunction implements HashFunction, StandaloneRepresentable {

    protected final String algorithm;

    AbstractSHAHashFunction(final String algorithm) {
        this.algorithm = algorithm;
    }

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

    @Override
    public byte[] hash(final byte[] bytes) {
        final SHAHashAccumulator accu = new SHAHashAccumulator(algorithm);
        accu.append(bytes);
        return accu.extractBytes();
    }
}
