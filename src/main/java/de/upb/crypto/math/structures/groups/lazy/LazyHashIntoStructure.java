package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.util.Objects;

public class LazyHashIntoStructure implements HashIntoStructure {
    @Represented
    protected HashIntoGroupImpl impl;
    @Represented
    protected LazyGroup target;

    public LazyHashIntoStructure(HashIntoGroupImpl hash, LazyGroup target) {
        this.impl = hash;
        this.target = target;
    }

    public LazyHashIntoStructure(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public LazyGroupElement hashIntoStructure(byte[] x) {
        return new HashResultLazyGroupElement(this, x);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LazyHashIntoStructure)) return false;
        LazyHashIntoStructure that = (LazyHashIntoStructure) o;
        return impl.equals(that.impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }
}