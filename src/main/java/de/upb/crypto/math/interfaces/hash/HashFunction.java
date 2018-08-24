package de.upb.crypto.math.interfaces.hash;

import de.upb.crypto.math.serialization.StandaloneRepresentable;

import java.nio.charset.StandardCharsets;

public interface HashFunction extends StandaloneRepresentable {

    /**
     * Returns the output-length of the HashFunction in bytes.
     *
     * @return the output-length in bytes
     */
    public int getOutputLength();

    /**
     * Hashes an UniqueByteRepresentable using the hash function.
     *
     * @param ubr the UBR to hash
     * @return the hash
     */
    default byte[] hash(UniqueByteRepresentable ubr) {
        return hash(ubr.getUniqueByteRepresentation());
    }

    /**
     * Hashes bytes using the hash function.
     *
     * @param bytes the bytes to hash
     * @return the hash
     */
    byte[] hash(byte[] bytes);

    /**
     * Hashes a String using the hash function
     *
     * @param string the string to hash
     * @return the hash
     */
    default byte[] hash(String string) {
        return hash(string.getBytes(StandardCharsets.UTF_8));
    }
}
