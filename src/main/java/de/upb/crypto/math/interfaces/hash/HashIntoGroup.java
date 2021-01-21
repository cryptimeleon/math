package de.upb.crypto.math.interfaces.hash;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.nio.charset.StandardCharsets;

/**
 * Represents a hash function that maps a {@code byte[]} to a {@link GroupElement} of a {@link Group}.
 */
public interface HashIntoGroup extends HashIntoStructure {

    @Override
    GroupElement hash(byte[] x);

    @Override
    default GroupElement hash(String x) {
        return hash(x.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    default GroupElement hash(UniqueByteRepresentable ubr) {
        return hash(ubr.getUniqueByteRepresentation());
    }
}
