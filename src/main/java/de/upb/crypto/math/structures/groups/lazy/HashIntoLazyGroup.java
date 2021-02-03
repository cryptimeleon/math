package de.upb.crypto.math.structures.groups.lazy;


import de.upb.crypto.math.structures.groups.HashIntoGroup;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import de.upb.crypto.math.structures.groups.mappings.impl.HashIntoGroupImpl;

import java.util.Objects;

/**
 * A {@link HashIntoGroupImpl} wrapper implementing deferred (lazy) evaluation.
 * <p>
 * Allows for additional optimizations using information about the operations being applied.
 * <p>
 * For more information, see the <a href="https://upbcuk.github.io/docs/lazy-eval.html">documentation</a>.
 */
public class HashIntoLazyGroup implements HashIntoGroup {
    @Represented
    protected HashIntoGroupImpl impl;
    @Represented
    protected LazyGroup target;

    public HashIntoLazyGroup(HashIntoGroupImpl hash, LazyGroup target) {
        this.impl = hash;
        this.target = target;
    }

    public HashIntoLazyGroup(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public LazyGroupElement hash(byte[] x) {
        return new HashResultLazyGroupElement(this, x);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashIntoLazyGroup)) return false;
        HashIntoLazyGroup that = (HashIntoLazyGroup) o;
        return impl.equals(that.impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }

    public LazyGroup getTarget() {
        return target;
    }
}
