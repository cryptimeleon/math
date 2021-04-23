package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.mappings.impl.GroupHomomorphismImpl;

import java.util.Objects;

/**
 * Implements an isomorphism between two {@link DebugGroupImpl}s.
 */
class DebugIsomorphismImpl implements GroupHomomorphismImpl {
    @Represented
    private DebugGroupImpl src;
    @Represented
    private DebugGroupImpl target;

    public DebugIsomorphismImpl(DebugGroupImpl src, DebugGroupImpl target) {
        this.src = src;
        this.target = target;
    }

    public DebugIsomorphismImpl(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public GroupElementImpl apply(GroupElementImpl groupElement) {
        if (!groupElement.getStructure().equals(src))
            throw new IllegalArgumentException("Tried to apply isomorphism on wrong group (argument was from " + groupElement.getStructure() + ")");
        return new DebugGroupElementImpl(target, ((DebugGroupElementImpl) groupElement).elem);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugIsomorphismImpl that = (DebugIsomorphismImpl) o;
        return Objects.equals(src, that.src) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, target);
    }
}
