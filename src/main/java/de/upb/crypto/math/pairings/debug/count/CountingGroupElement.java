package de.upb.crypto.math.pairings.debug.count;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyGroupElement;

import java.math.BigInteger;
import java.util.Objects;

public class CountingGroupElement implements GroupElement {

    @Represented
    protected CountingGroup group;

    @Represented
    protected LazyGroupElement elemTotal;

    @Represented
    protected LazyGroupElement elemExpMultiExp;

    public CountingGroupElement(CountingGroup group, LazyGroupElement elemTotal, LazyGroupElement elemExpMultiExp) {
        this.group = group;
        this.elemTotal = elemTotal;
        this.elemExpMultiExp = elemExpMultiExp;
    }

    public CountingGroupElement(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Group getStructure() {
        return new CountingGroup(
                (LazyGroup) elemTotal.getStructure(),
                (LazyGroup) elemExpMultiExp.getStructure()
        );
    }

    @Override
    public GroupElement inv() {
        return new CountingGroupElement(
                group,
                (LazyGroupElement) elemTotal.inv(),
                (LazyGroupElement) elemExpMultiExp.inv()
        );
    }

    @Override
    public GroupElement op(Element e) throws IllegalArgumentException {
        if (e == null)
            throw new IllegalArgumentException("Argument element is null");
        if (e.getClass() != this.getClass())
            throw new IllegalArgumentException("Argument element is not a CountingGroupElement");
        CountingGroupElement other = (CountingGroupElement) e;
        return new CountingGroupElement(
                group,
                (LazyGroupElement) elemTotal.op(other.elemTotal),
                (LazyGroupElement) elemExpMultiExp.op(other.elemExpMultiExp)
        );
    }

    @Override
    public GroupElement pow(BigInteger exponent) {
        return new CountingGroupElement(
                group,
                (LazyGroupElement) elemTotal.pow(exponent),
                (LazyGroupElement) elemExpMultiExp.pow(exponent)
        );
    }

    @Override
    public GroupElement precomputePow() {
        return new CountingGroupElement(
                group,
                (LazyGroupElement) elemTotal.precomputePow(),
                (LazyGroupElement) elemExpMultiExp.precomputePow()
        );
    }

    @Override
    public GroupElement compute() {
        // counting requires synchronization so we always do computeSync
        return new CountingGroupElement(
                group,
                (LazyGroupElement) elemTotal.computeSync(),
                (LazyGroupElement) elemExpMultiExp.computeSync()
        );
    }

    @Override
    public GroupElement computeSync() {
        return new CountingGroupElement(
                group,
                (LazyGroupElement) elemTotal.computeSync(),
                (LazyGroupElement) elemExpMultiExp.computeSync()
        );
    }

    @Override
    public boolean isComputed() {
        return elemTotal.isComputed() && elemExpMultiExp.isComputed();
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return elemExpMultiExp.updateAccumulator(elemTotal.updateAccumulator(accumulator));
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountingGroupElement other = (CountingGroupElement) o;
        return Objects.equals(group, other.group)
                && Objects.equals(elemTotal, other.elemTotal)
                && Objects.equals(elemExpMultiExp, other.elemExpMultiExp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, elemTotal, elemExpMultiExp);
    }

    @Override
    public String toString() {
        return group + " element " + elemTotal.computeSync().getRepresentation() + ";"
                + elemExpMultiExp.computeSync().getRepresentation();
    }
}
