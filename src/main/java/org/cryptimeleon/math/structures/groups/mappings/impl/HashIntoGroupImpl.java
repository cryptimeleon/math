package org.cryptimeleon.math.structures.groups.mappings.impl;

import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;

import java.nio.charset.StandardCharsets;

/**
 * Interface for implementing a hash function to some group.
 */
public interface HashIntoGroupImpl extends StandaloneRepresentable {
    /**
     * Hashes a byte array into the configured group.
     *
     * @param x a sequence of bytes to hash
     * @return a group element
     */
    GroupElementImpl hashIntoGroupImpl(byte[] x);


    default GroupElementImpl hashIntoGroupImpl(UniqueByteRepresentable ubr) {
        return hashIntoGroupImpl(ubr.getUniqueByteRepresentation());
    }

    /**
     * Hashes a String (UTF-8 encoded) into the configured group.
     *
     * @param x a String
     * @return an element
     */
    default GroupElementImpl hashIntoGroupImpl(String x) {
        return hashIntoGroupImpl(x.getBytes(StandardCharsets.UTF_8));
    }
}
