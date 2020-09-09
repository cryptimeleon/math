package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Base interface for Structures that contain Elements
 * <p>
 * Implementations should override equals() and hashCode()
 * Structures implement StandaloneRepresentable, i.e. they must supply a constructor with a single Representation argument
 * (and hence, their Representation must contain all the information necessary to recreate the structure)
 */
public interface Structure extends StandaloneRepresentable {
    /**
     * Size of the structure
     *
     * @return the number of elements contained in this structure or null if infinite
     * @throws UnsupportedOperationException if the number of elements is unknown / too expensive to compute
     */
    BigInteger size() throws UnsupportedOperationException;

    /**
     * Returns true if the size of this structure is known and prime.
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
     * Returns an element of this structure that is drawn uniformly at random (using a cryptographically strong RNG)
     *
     * @throws UnsupportedOperationException
     */
    Element getUniformlyRandomElement() throws UnsupportedOperationException;

    /**
     * Returns n elements of this structure that are drawn uniformly and independently at random (using a cryptographically strong RNG)
     *
     * @throws UnsupportedOperationException
     */
    Vector<? extends Element> getUniformlyRandomElements(int n) throws UnsupportedOperationException;

    /**
     * Creates an Element of this Structure from its representation.
     *
     * @param repr the Representation returned by Element.getRepresentation()
     * @return the unique Element encoded by 'repr'
     */
    Element getElement(Representation repr);

    /**
     * Returns the number of bytes returned by this Structure's {@link Element}s' {@link Element#getUniqueByteRepresentation()},
     * or an empty Optional if this structure's elements do not guarantee a fixed length.
     * For example, elements of Zp will always be represented by ceil(ceil(log(p))/8) bytes, hence getUniqueByteLength() would return ceil(ceil(log(p))/8).
     * A polynomial ring would return null since a polynomial's unique byte representation length depends on its degree.
     *
     * @return the guaranteed fixed length of element.getUniqueByteRepresentation().size(), or an empty Optional, if no guarantee.
     */
    Optional<Integer> getUniqueByteLength();
}
