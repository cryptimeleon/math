package de.upb.crypto.math.structures.groups.basic;

import de.upb.crypto.math.structures.groups.mappings.GroupHomomorphism;
import de.upb.crypto.math.structures.groups.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.structures.groups.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;

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
