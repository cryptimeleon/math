package de.upb.crypto.math.structures.groups.basic;

import de.upb.crypto.math.interfaces.hash.HashIntoGroup;
import de.upb.crypto.math.interfaces.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

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
