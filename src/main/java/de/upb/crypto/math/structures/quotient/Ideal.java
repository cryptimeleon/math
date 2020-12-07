package de.upb.crypto.math.structures.quotient;

import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.Representable;

import java.util.List;

/**
 * An ideal that is finitely generated.
 */
public abstract class Ideal implements Representable {
    /**
     * Checks whether the given {@link RingElement} is contained in this ideal.
     */
    public abstract boolean isMember(RingElement e);

    /**
     * Returns the list of ring elements that generate this ideal.
     * <p>
     * The generators are the elements \(x_1, x_2, ..., x_n\) such that any element y in this ideal can be
     * represented as a linear combination of those \(x_i\).
     */
    public abstract List<RingElement> getGenerators();

    @Override
    public abstract boolean equals(Object obj);
}
