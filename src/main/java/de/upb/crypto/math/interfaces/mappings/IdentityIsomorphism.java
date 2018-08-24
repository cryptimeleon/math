package de.upb.crypto.math.interfaces.mappings;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;

public class IdentityIsomorphism implements GroupHomomorphism {

    public IdentityIsomorphism() {

    }

    public IdentityIsomorphism(Representation repr) {
        this(); //don't need representation
    }

    @Override
    public GroupElement apply(GroupElement t) {
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
    public boolean equals(Object o) {
        return (o instanceof IdentityIsomorphism);
    }
}
