package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.util.Objects;

public class DebugIsomorphismImpl implements GroupHomomorphismImpl {
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
