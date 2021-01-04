package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.basic.BasicGroup;
import de.upb.crypto.math.structures.groups.basic.BasicGroupElement;

import java.util.Objects;

/**
 * A {@link GroupHomomorphismImpl} wrapper implementing deferred (lazy) evaluation.
 * <p>
 * Allows for additional optimizations using information about the operations being applied.
 * <p>
 * For more information, see the <a href="https://upbcuk.github.io/docs/lazy-eval.html">documentation</a>.
 */
public class LazyGroupHomomorphism implements GroupHomomorphism {
    @Represented
    protected LazyGroup targetGroup;
    @Represented
    protected GroupHomomorphismImpl impl;

    public LazyGroupHomomorphism(LazyGroup targetGroup, GroupHomomorphismImpl homomorphismImpl) {
        this.targetGroup = targetGroup;
        this.impl = homomorphismImpl;
    }

    public LazyGroupHomomorphism(Representation repr) {
        ReprUtil.deserialize(this, repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public HomomorphismResultLazyGroupElement apply(GroupElement groupElement) {
        return new HomomorphismResultLazyGroupElement((LazyGroupElement) groupElement, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LazyGroupHomomorphism that = (LazyGroupHomomorphism) o;
        return impl.equals(that.impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }

    public LazyGroup getTargetGroup() {
        return targetGroup;
    }
}
