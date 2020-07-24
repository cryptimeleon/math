package de.upb.crypto.math.interfaces.mappings.impl;

import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

import java.nio.charset.StandardCharsets;

public interface HashIntoGroupImpl extends StandaloneRepresentable {
    /**
     * Hashes a byte array into the Structure.
     *
     * @param x a sequence of bytes to hash
     * @return an element
     */
    GroupElementImpl hashIntoGroupImpl(byte[] x);


    default GroupElementImpl hashIntoGroupImpl(UniqueByteRepresentable ubr) {
        return hashIntoGroupImpl(ubr.getUniqueByteRepresentation());
    }

    /**
     * Hashes a String (UTF-8 encoded) into the Structure.
     *
     * @param x a String
     * @return an element
     */
    default GroupElementImpl hashIntoGroupImpl(String x) {
        return hashIntoGroupImpl(x.getBytes(StandardCharsets.UTF_8));
    }
}
