package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.mappings.impl.HashIntoGroupImpl;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZn;

import java.util.Objects;

/**
 * Allows hashing a byte array to a {@link DebugGroupImpl} via {@link HashIntoZn}.
 */
class HashIntoDebugGroupImpl implements HashIntoGroupImpl {
    protected DebugGroupImpl group;
    protected HashIntoZn hash;

    public HashIntoDebugGroupImpl(DebugGroupImpl group) {
        this.group = group;
        this.hash = new HashIntoZn(group.size());
    }

    public HashIntoDebugGroupImpl(Representation repr) {
        this(new DebugGroupImpl(repr));
    }

    @Override
    public Representation getRepresentation() {
        return group.getRepresentation();
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
