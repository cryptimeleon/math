package de.upb.crypto.math.interfaces.hash;

import de.upb.crypto.math.hash.annotations.AnnotatedUbrUtil;
import de.upb.crypto.math.hash.impl.ByteArrayAccumulator;
import de.upb.crypto.math.serialization.Representable;

/**
 * Interface for all objects that have unique and consistant representations as byte arrays.
 * Such objects can, for example, be used as input to a {@link HashFunction}.
 * Unlike in the {@link Representable} interface, all UniqueByteRepresentables are guaranteed to output the same UniqueByteRepresentation if the object is the same.
 * If two objects are not the same, but are of the same type, their unique byte representation must differ (the mapping is injective).
 * (For the sake of sanity and performance, "of the same type" here means also that they are part of the same context,
 * e.g., based on the same group / use the same public parameters)
 *
 * @author Mirko Juergens
 */
public interface UniqueByteRepresentable {
    /**
     * Updates the ByteAccumulator with the unique byte representation of this object.
     * The input to the accumulators update function is an injective (with respect to a given domain) byte encoding of this object.
     * <p>
     * For many use-cases, the {@link AnnotatedUbrUtil} can be used to quickly implement this method.
     *
     * @return the same accumulator as was input
     */
    ByteAccumulator updateAccumulator(ByteAccumulator accumulator);

    /**
     * An injective mapping of the domain of this object to byte[].
     * Shorthand for updateAccumulator(new ByteArrayAccumulator()).extractBytes().
     * (Implementors of the UniqueByteRepresentable interface should override updateAccumulator(ByteAccumulatr) instead)
     */
    default byte[] getUniqueByteRepresentation() {
        return updateAccumulator(new ByteArrayAccumulator()).extractBytes();
    }
}