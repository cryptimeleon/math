package de.upb.crypto.math.interfaces.hash;

import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Represents a hash function that maps byte[] -> Element
 */
public interface HashIntoStructure extends StandaloneRepresentable {
    /**
     * Hashes a byte array into the Structure.
     *
     * @param x a sequence of bytes to hash
     * @return an element
     */
    Element hashIntoStructure(byte[] x);


    default Element hashIntoStructure(UniqueByteRepresentable ubr) {
        return hashIntoStructure(ubr.getUniqueByteRepresentation());
    }

    /**
     * Hashes a String (UTF-8 encoded) into the Structure.
     *
     * @param x a String
     * @return an element
     */
    default Element hashIntoStructure(String x) {
        return hashIntoStructure(x.getBytes(StandardCharsets.UTF_8));
    }
}
