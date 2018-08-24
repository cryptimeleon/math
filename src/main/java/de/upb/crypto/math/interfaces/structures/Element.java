package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.serialization.Representable;


/**
 * Elements are immutable objects that are connected to some Structure.
 * <p>
 * Implementations are required to implement equals() and hashCode() such that x.equals(y) implies x.hashCode() == y.hashCode().
 * The hashCode() implementation does not have to be collision resistant.
 * Generally, two elements are only considered equal if they belong to equal structures.
 */
public interface Element extends Representable, UniqueByteRepresentable {
    public static final String RECOVERY_METHOD = "getElement";

    /**
     * Returns the Structure that this Element belongs to
     */
    Structure getStructure();

    @Override
    public boolean equals(Object obj); //see Javadoc above

    @Override
    public int hashCode(); //see Javadoc above
}
