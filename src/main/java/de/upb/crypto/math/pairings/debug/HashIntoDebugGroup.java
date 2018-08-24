package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.HashIntoZn;

import java.util.Objects;

public class HashIntoDebugGroup implements HashIntoStructure {
    protected DebugGroup group;
    protected HashIntoZn hash;

    public HashIntoDebugGroup(DebugGroup group) {
        this.group = group;
        this.hash = new HashIntoZn(group.size());
    }

    public HashIntoDebugGroup(Representation repr) {
        this(new DebugGroup(repr));
    }

    @Override
    public DebugGroupElement hashIntoStructure(byte[] x) {
        DebugGroupLogger.log(group.name, "hashInto");
        return group.wrap(hash.hashIntoStructure(x));
    }

    @Override
    public Representation getRepresentation() {
        return group.getRepresentation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashIntoDebugGroup that = (HashIntoDebugGroup) o;
        return Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group);
    }
}
