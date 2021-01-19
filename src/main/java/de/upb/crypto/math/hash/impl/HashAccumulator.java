package de.upb.crypto.math.hash.impl;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.hash.HashFunction;

/**
 * Marker interface for accumulators that are used to implement {@link HashFunction}s.
 * <p>
 * Specifically, it's a {@link ByteAccumulator} that outputs the hash value of its input in {@link #extractBytes()}.
 *
 */
public abstract class HashAccumulator extends ByteAccumulator {

}
