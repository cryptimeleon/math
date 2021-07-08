package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.mappings.impl.HashIntoGroupImpl;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZn;

import java.util.Objects;

/**
 * Allows hashing a byte array to a {@link DebugGroupImpl} via {@link HashIntoZn}.
 */
public class HashIntoDebugGroupImpl implements HashIntoGroupImpl {
    @Represented
    protected DebugGroupImpl group;
    protected HashIntoZn hash;

    public HashIntoDebugGroupImpl(DebugGroupImpl group) {
        this.group = group;
        this.hash = new HashIntoZn(group.size());
    }

    public HashIntoDebugGroupImpl(Representation repr) {
        new ReprUtil(this).deserialize(repr);
        hash = new HashIntoZn(group.size());
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashIntoDebugGroupImpl that = (HashIntoDebugGroupImpl) o;
        return Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group);
    }

    @Override
    public GroupElementImpl hashIntoGroupImpl(byte[] x) {
        return group.wrap(hash.hash(x));
    }
}
