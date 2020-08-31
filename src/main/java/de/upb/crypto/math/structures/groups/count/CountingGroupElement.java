package de.upb.crypto.math.structures.groups.count;

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
    LazyGroupElement elemTotal;

    @Represented
    LazyGroupElement elemExpMultiExp;

    public CountingGroupElement(LazyGroupElement elemTotal, LazyGroupElement elemExpMultiExp) {
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
                (LazyGroupElement) elemTotal.op(other.elemTotal),
                (LazyGroupElement) elemExpMultiExp.op(other.elemExpMultiExp)
        );
    }

    @Override
    public GroupElement pow(BigInteger exponent) {
        return new CountingGroupElement(
                (LazyGroupElement) elemTotal.pow(exponent),
                (LazyGroupElement) elemExpMultiExp.pow(exponent)
        );
    }

    @Override
    public GroupElement precomputePow() {
        return new CountingGroupElement(
                (LazyGroupElement) elemTotal.precomputePow(),
                (LazyGroupElement) elemExpMultiExp.precomputePow()
        );
    }

    @Override
    public GroupElement compute() {
        // counting requires synchronization so we always do computeSync
        return new CountingGroupElement(
                (LazyGroupElement) elemTotal.computeSync(),
                (LazyGroupElement) elemExpMultiExp.computeSync()
        );
    }

    @Override
    public GroupElement computeSync() {
        return new CountingGroupElement(
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
        // TODO: Is this correct?
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
        CountingGroupElement that = (CountingGroupElement) o;
        return Objects.equals(elemTotal, that.elemTotal)
                && Objects.equals(elemExpMultiExp, that.elemExpMultiExp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elemTotal, elemExpMultiExp);
    }
}
