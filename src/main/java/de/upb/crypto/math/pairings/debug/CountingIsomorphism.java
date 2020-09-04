package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.util.Objects;

public class CountingIsomorphism implements GroupHomomorphism {

    @Represented
    private CountingGroup src;
    @Represented
    private CountingGroup target;

    public CountingIsomorphism(CountingGroup src, CountingGroup target) {
        this.src = src;
        this.target = target;
    }

    public CountingIsomorphism(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public GroupElement apply(GroupElement groupElement) {
        if (!groupElement.getStructure().equals(src)) {
            throw new IllegalArgumentException("Tried to apply isomorphism on element from wrong group" +
                    " (argument is from " + groupElement.getStructure() + ")");
        }
        DebugGroupElementImpl debugElem = (DebugGroupElementImpl) ((CountingGroupElement) groupElement).elemTotal.getConcreteGroupElement();
        return target.wrap(debugElem.elem);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountingIsomorphism that = (CountingIsomorphism) o;
        return Objects.equals(src, that.src) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, target);
    }
}
