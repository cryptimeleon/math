package org.cryptimeleon.math.structures.groups.basic;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.mappings.GroupHomomorphism;
import org.cryptimeleon.math.structures.groups.mappings.impl.GroupHomomorphismImpl;

import java.util.Objects;

/**
 * A basic {@link GroupHomomorphismImpl} wrapper where operations are evaluated naively, i.e. operation by operation.
 */
public class BasicGroupHomomorphism implements GroupHomomorphism {
    @Represented
    protected BasicGroup targetGroup;
    @Represented
    protected GroupHomomorphismImpl impl;

    public BasicGroupHomomorphism(BasicGroup targetGroup, GroupHomomorphismImpl homomorphismImpl) {
        this.targetGroup = targetGroup;
        this.impl = homomorphismImpl;
    }

    public BasicGroupHomomorphism(Representation repr) {
        ReprUtil.deserialize(this, repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public GroupElement apply(GroupElement groupElement) {
        return targetGroup.wrap(impl.apply(((BasicGroupElement) groupElement).impl));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicGroupHomomorphism that = (BasicGroupHomomorphism) o;
        return targetGroup.equals(that.targetGroup) &&
                impl.equals(that.impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }
}
