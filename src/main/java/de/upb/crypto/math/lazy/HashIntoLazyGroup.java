package de.upb.crypto.math.lazy;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.AnnotatedRepresentationUtil;
import de.upb.crypto.math.serialization.annotations.Represented;

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
        AnnotatedRepresentationUtil.restoreAnnotatedRepresentation(repr, this);
    }

    @Override
    public Element hashIntoStructure(byte[] x) {
        return new LeafGroupElement(group, (GroupElement) baseHash.hashIntoStructure(x));
    }

    @Override
    public Representation getRepresentation() {
        return AnnotatedRepresentationUtil.putAnnotatedRepresentation(this);
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
