package org.cryptimeleon.math.hash.impl;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.hash.HashFunction;

/**
 * Marker interface for accumulators that are used to implement {@link HashFunction}s.
 * <p>
 * Specifically, it's a {@link ByteAccumulator} that outputs the hash value of its input in {@link #extractBytes()}.
 *
 */
public abstract class HashAccumulator extends ByteAccumulator {

}
