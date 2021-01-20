package de.upb.crypto.math.structures;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.structures.cartesian.Vector;
import de.upb.crypto.math.structures.rings.zn.Zp;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Base interface for algebraic structures that contain elements.
 * <p>
 * Implementations should override  {@code equals()} and {@code hashCode()}.
 * <p>
 * Structures implement {@link StandaloneRepresentable},
 * i.e. they must supply a constructor with a single {@link Representation} argument.
 * Hence, their Representation must contain all the information necessary to recreate the structure.
 */
public interface Structure extends StandaloneRepresentable {
    /**
     * Returns the number of elements in this structure (the size).
     *
     * @return the number of elements contained in this structure or null if infinite
     * @throws UnsupportedOperationException if the number of elements is unknown or too expensive to compute
     */
    BigInteger size() throws UnsupportedOperationException;

    /**
     * Checks if the structure has prime size.
     *
     * @return true if the structure has prime size, else false.
     * @throws UnsupportedOperationException if the primality of the size cannot be determined
     */
    default boolean hasPrimeSize() {
        try {
            BigInteger size = size();
            if (size == null) //infinite size
                return false;
            return size.isProbablePrime(80);
        } catch (UnsupportedOperationException ex) {
            return false;
        }
    }

    /**
     * Returns an element of this structure that is drawn uniformly at random
     * using a cryptographically strong RNG.
     *
     * @throws UnsupportedOperationException if the operation is not supported
     */
    Element getUniformlyRandomElement() throws UnsupportedOperationException;

    /**
     * Returns n elements of this structure that are drawn uniformly and independently at random
     * using a cryptographically strong RNG.
     *
     * @throws UnsupportedOperationException if the operation is not supported
     */
    Vector<? extends Element> getUniformlyRandomElements(int n) throws UnsupportedOperationException;

    /**
     * Creates an element of this structure from its representation.
     *
     * @param repr the {@code Representation} returned by {@link Element#getRepresentation()}
     * @return the decoded element corresponding to the representation
     */
    Element getElement(Representation repr);

    /**
     * Returns the number of bytes returned by this structure's {@link Element#getUniqueByteRepresentation()},
     * or an empty {@code Optional} if this structure's elements do not guarantee a fixed length.
     * <p>
     * For example, elements of {@link Zp} will always be represented by {@code ceil(ceil(log(p))/8)} bytes,
     * hence {@code getUniqueByteLength()} would return {@code ceil(ceil(log(p))/8)}.
     * <p>
     * A polynomial ring would return an empty {@code Optional} since a polynomial's unique byte representation length
     * depends on its degree.
     *
     * @return the guaranteed fixed length of {@code getUniqueByteRepresentation()},
     *         or an empty {@code Optional}, if no guarantee
     */
    Optional<Integer> getUniqueByteLength();
}
