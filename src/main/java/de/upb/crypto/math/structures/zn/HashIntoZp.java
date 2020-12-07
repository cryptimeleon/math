package de.upb.crypto.math.structures.zn;

import de.upb.crypto.math.interfaces.hash.HashFunction;
import de.upb.crypto.math.serialization.Representation;

/**
 * A hash function that maps to {@link Zp}.
 * <p>
 * This is a copy of {@link HashIntoZn}, however, it returns {@code ZpElement}'s.
 */
public class HashIntoZp extends HashIntoZn {

    public HashIntoZp(HashFunction hashFunction, Zp zp) {
        super(hashFunction, zp);
    }

    public HashIntoZp(Zp zp) {
        super(zp);
    }

    /**
     * Reconstructs the hash function from its representation.
     */
    public HashIntoZp(Representation repr) {
        super(repr);
    }

    @Override
    public Zp.ZpElement hashIntoStructure(byte[] x) {
        return (Zp.ZpElement) super.hashIntoStructure(x);
    }

    /**
     * Returns the ring {@code Zp} that this function hashes into.
     */
    public Zp getTargetStructure() {
        return (Zp) structure;
    }
}