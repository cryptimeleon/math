package de.upb.crypto.math.pairings.counting;

import de.upb.crypto.math.interfaces.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.HashIntoZn;

import java.util.Objects;

/**
 * Allows hashing a byte array to a {@link CountingGroupImpl} via {@link HashIntoZn}.
 */
public class HashIntoCountingGroupImpl implements HashIntoGroupImpl {
    protected CountingGroupImpl group;
    protected HashIntoZn hash;

    public HashIntoCountingGroupImpl(CountingGroupImpl group) {
        this.group = group;
        this.hash = new HashIntoZn(group.size());
    }

    public HashIntoCountingGroupImpl(Representation repr) {
        this(new CountingGroupImpl(repr));
    }

    @Override
    public Representation getRepresentation() {
        return group.getRepresentation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashIntoCountingGroupImpl that = (HashIntoCountingGroupImpl) o;
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
