package de.upb.crypto.math.hash;

import de.upb.crypto.math.hash.annotations.AnnotatedUbrUtil;
import de.upb.crypto.math.hash.impl.ByteArrayAccumulator;
import de.upb.crypto.math.serialization.Representable;

/**
 * Interface for all objects that have a unique and consistent representation as byte arrays.
 * <p>
 * Such objects can, for example, be used as input to a {@link HashFunction}.
 * <p>
 * Unlike in the {@link Representable} interface, all {@code UniqueByteRepresentable} instances are guaranteed to
 * be mapped to the same byte representation if the object is the same.
 * If two objects are not the same, but are of the same type, their unique byte representation must differ,
 * i.e. the mapping is injective.
 * (For the sake of sanity and performance, "of the same type" here means also that they are part of the same context,
 * e.g., based on the same group / use the same public parameters)
 *
 */
public interface UniqueByteRepresentable {
    /**
     * Updates the ByteAccumulator with the unique byte representation of this object.
     * <p>
     * The input to the accumulators update function is an injective (with respect to a given domain) byte encoding of
     * this object.
     * <p>
     * For many use-cases, the {@link AnnotatedUbrUtil} can be used to quickly implement this method.
     *
     * @return the same accumulator as was input
     */
    ByteAccumulator updateAccumulator(ByteAccumulator accumulator);

    /**
     * An injective mapping of the domain of this object to a {@code byte[]}.
     * <p>
     * Shorthand for {@code updateAccumulator(new ByteArrayAccumulator()).extractBytes()}.
     * <p>
     * Implementors of the {@code UniqueByteRepresentable} interface should override
     * {@code updateAccumulator(ByteAccumulator)} instead.
     */
    default byte[] getUniqueByteRepresentation() {
        return updateAccumulator(new ByteArrayAccumulator()).extractBytes();
    }
}