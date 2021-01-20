package de.upb.crypto.math.structures.groups.counting;

import de.upb.crypto.math.structures.groups.mappings.GroupHomomorphism;
import de.upb.crypto.math.structures.groups.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyGroupHomomorphism;

import java.util.Objects;

/**
 * A homomorphism between two {@link CountingGroup}s.
 */
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
                totalHom.apply(((CountingGroupElement) groupElement).elemTotal),
                expMultiExpHom.apply(((CountingGroupElement) groupElement).elemExpMultiExp)
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
