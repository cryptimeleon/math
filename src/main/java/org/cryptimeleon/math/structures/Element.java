package org.cryptimeleon.math.structures;

import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.Representable;


/**
 * Elements are immutable objects that are connected to some {@link Structure}.
 * <p>
 * Implementations are required to implement {@code equals()} and {@code hashCode()}
 * such that {@code x.equals(y)} implies {@code x.hashCode() == y.hashCode()}.
 * The {@code hashCode()} implementation does not have to be collision resistant.
 * <p>
 * Generally, two elements are only considered equal if they belong to equal structures.
 */
public interface Element extends Representable, UniqueByteRepresentable {

    /**
     * Returns the {@code Structure} that this {@code Element} belongs to.
     */
    Structure getStructure();

    @Override
    boolean equals(Object obj); //see Javadoc above

    @Override
    int hashCode(); //see Javadoc above
}
