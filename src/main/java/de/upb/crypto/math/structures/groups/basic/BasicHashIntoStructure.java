package de.upb.crypto.math.structures.groups.basic;

import de.upb.crypto.math.hash.HashIntoStructure;
import de.upb.crypto.math.structures.groups.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;

import java.util.Objects;

/**
 * A basic {@link HashIntoGroupImpl} wrapper where operations are evaluated naively, i.e. operation by operation.
 */
public class BasicHashIntoStructure implements HashIntoStructure {
    @Represented
    protected HashIntoGroupImpl impl;
    @Represented
    protected BasicGroup target;

    public BasicHashIntoStructure(HashIntoGroupImpl hash, BasicGroup target) {
        this.impl = hash;
        this.target = target;
    }

    public BasicHashIntoStructure(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public BasicGroupElement hashIntoStructure(byte[] x) {
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
        BasicHashIntoStructure that = (BasicHashIntoStructure) o;
        return impl.equals(that.impl) &&
                target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }
}
