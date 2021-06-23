package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroupHomomorphism;
import org.cryptimeleon.math.structures.groups.mappings.GroupHomomorphism;

import java.util.Objects;

/**
 * A homomorphism between two {@link DebugGroup}s.
 */
public class DebugHomomorphism implements GroupHomomorphism {

    @Represented
    private LazyGroupHomomorphism totalHom;
    @Represented
    private LazyGroupHomomorphism expMultiExpHom;

    public DebugHomomorphism(LazyGroupHomomorphism totalHom, LazyGroupHomomorphism expMultiExpHom) {
        this.totalHom = totalHom;
        this.expMultiExpHom = expMultiExpHom;
    }

    public DebugHomomorphism(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public GroupElement apply(GroupElement groupElement) {
        return new DebugGroupElement(
                new DebugGroup(totalHom.getTargetGroup(), expMultiExpHom.getTargetGroup()),
                totalHom.apply(((DebugGroupElement) groupElement).elemTotal),
                expMultiExpHom.apply(((DebugGroupElement) groupElement).elemExpMultiExp)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugHomomorphism that = (DebugHomomorphism) o;
        return Objects.equals(totalHom, that.totalHom) &&
                Objects.equals(expMultiExpHom, that.expMultiExpHom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalHom, expMultiExpHom);
    }
}
