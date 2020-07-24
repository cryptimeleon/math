package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.HashIntoZn;

import java.util.Objects;

public class HashIntoDebugGroupImpl implements HashIntoGroupImpl {
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
        return group.wrap(hash.hashIntoStructure(x));
    }
}
