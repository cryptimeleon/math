package de.upb.crypto.math.structures.quotient;

import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.Representable;

import java.util.List;

/**
 * An ideal that is finitely generated
 */
public abstract class Ideal implements Representable {
    public abstract boolean isMember(RingElement e);

    public abstract List<RingElement> getGenerators();

    @Override
    public abstract boolean equals(Object obj);
}
