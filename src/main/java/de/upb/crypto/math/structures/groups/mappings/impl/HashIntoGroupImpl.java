package de.upb.crypto.math.structures.groups.mappings.impl;

import de.upb.crypto.math.hash.UniqueByteRepresentable;
import de.upb.crypto.math.structures.groups.GroupElementImpl;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

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
