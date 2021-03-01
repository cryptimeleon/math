package org.cryptimeleon.math.structures.groups.basic;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.HashIntoGroup;
import org.cryptimeleon.math.structures.groups.mappings.impl.HashIntoGroupImpl;

import java.util.Objects;

/**
 * A basic {@link HashIntoGroupImpl} wrapper where operations are evaluated naively, i.e. operation by operation.
 */
public class HashIntoBasicGroup implements HashIntoGroup {
    @Represented
    protected HashIntoGroupImpl impl;
    @Represented
    protected BasicGroup target;

    public HashIntoBasicGroup(HashIntoGroupImpl hash, BasicGroup target) {
        this.impl = hash;
        this.target = target;
    }

    public HashIntoBasicGroup(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public BasicGroupElement hash(byte[] x) {
        return target.wrap(impl.hashIntoGroupImpl(x));
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashIntoBasicGroup that = (HashIntoBasicGroup) o;
        return impl.equals(that.impl) &&
                target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }
}
