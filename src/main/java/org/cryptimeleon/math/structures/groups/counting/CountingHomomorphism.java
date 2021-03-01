package org.cryptimeleon.math.structures.groups.counting;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroupHomomorphism;
import org.cryptimeleon.math.structures.groups.mappings.GroupHomomorphism;

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
