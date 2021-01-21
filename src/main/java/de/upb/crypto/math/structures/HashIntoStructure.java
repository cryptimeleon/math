package de.upb.crypto.math.structures;

import de.upb.crypto.math.hash.UniqueByteRepresentable;
import de.upb.crypto.math.structures.Element;
import de.upb.crypto.math.structures.Structure;
import de.upb.crypto.math.serialization.StandaloneRepresentable;

import java.nio.charset.StandardCharsets;

/**
 * Represents a hash function that maps a {@code byte[]} to an {@link Element} of a {@link Structure}.
 */
public interface HashIntoStructure extends StandaloneRepresentable {
    /**
     * Hashes a byte array into the structure.
     *
     * @param x a sequence of bytes to hash
     * @return the resulting structure element
     */
    Element hash(byte[] x);


    /**
     * Hashes a {@link UniqueByteRepresentable} in to the structure.
     *
     * @param ubr the {@code UniqueByteRepresentable} to hash
     * @return the resulting structure element
     */
    default Element hash(UniqueByteRepresentable ubr) {
        return hash(ubr.getUniqueByteRepresentation());
    }

    /**
     * Hashes a {@code String} (UTF-8 encoded) into the structure.
     *
     * @param x a {@code String}
     * @return the resulting structure element
     */
    default Element hash(String x) {
        return hash(x.getBytes(StandardCharsets.UTF_8));
    }
}
