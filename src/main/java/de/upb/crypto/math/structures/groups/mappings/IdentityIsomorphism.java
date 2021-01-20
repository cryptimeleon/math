package de.upb.crypto.math.structures.groups.mappings;

import de.upb.crypto.math.structures.groups.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.structures.groups.GroupElementImpl;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;

/**
 * Implements a group isomorphism using the identity function, i.e. {@code apply(g).equals(g)}.
 */
public class IdentityIsomorphism implements GroupHomomorphismImpl {

    public IdentityIsomorphism() {

    }

    public IdentityIsomorphism(Representation repr) {
        this(); //don't need representation
    }

    /**
     * Applies the isomorphism.
     */
    @Override
    public GroupElementImpl apply(GroupElementImpl t) {
        return t;
    }

    @Override
    public Representation getRepresentation() {
        return new ObjectRepresentation();
    }

    @Override
    public int hashCode() {
        return 17;
    }

    @Override
    public boolean equals(Object other) {
        return this.getClass() == other.getClass();
    }
}
