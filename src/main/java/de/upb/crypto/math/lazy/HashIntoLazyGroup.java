package de.upb.crypto.math.lazy;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.util.Objects;

public class HashIntoLazyGroup implements HashIntoStructure {
    @Represented
    protected HashIntoStructure baseHash;
    @Represented
    protected LazyGroup group;

    public HashIntoLazyGroup(LazyGroup group, HashIntoStructure baseHash) {
        this.group = group;
        this.baseHash = baseHash;
    }

    public HashIntoLazyGroup(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Element hashIntoStructure(byte[] x) {
        return new LazyGroupElement(group, (GroupElement) baseHash.hashIntoStructure(x));
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashIntoLazyGroup that = (HashIntoLazyGroup) o;
        return Objects.equals(baseHash, that.baseHash) &&
                Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHash, group);
    }
}
