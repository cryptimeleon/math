package de.upb.crypto.math.hash.impl;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.hash.HashFunction;

/**
 * Marker interface for accumulators, e.g. subclasses of {@link ByteAccumulator}, that are used to implement
 * {@link HashFunction}s.
 * <p>
 * That is: it's a {@link ByteAccumulator} that outputs the hash value of its input in extractBytes.
 *
 * @author Denis Diemert
 */
public abstract class HashAccumulator extends ByteAccumulator {

}
