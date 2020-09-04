package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyGroupHomomorphism;

import java.util.Objects;

public class CountingHomomorphism implements GroupHomomorphism {

    @Represented
    private LazyGroupHomomorphism totalHom;
    @Represented
    private LazyGroupHomomorphism expMultiExpHom;

    public CountingHomomorphism(LazyGroupHomomorphism totalHom, LazyGroupHomomorphism expMultiExpHom) {
        this.totalHom = totalHom;
        this.expMultiExpHom = expMultiExpHom;
    }

    public CountingHomomorphism(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public GroupElement apply(GroupElement groupElement) {
        return new CountingGroupElement(
                new CountingGroup(totalHom.getTargetGroup(), expMultiExpHom.getTargetGroup()),
                totalHom.apply(groupElement),
                expMultiExpHom.apply(groupElement)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountingHomomorphism that = (CountingHomomorphism) o;
        return Objects.equals(totalHom, that.totalHom) &&
                Objects.equals(expMultiExpHom, that.expMultiExpHom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalHom, expMultiExpHom);
    }
}
