package de.upb.crypto.math.structures.groups.count;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.math.BigInteger;
import java.util.Objects;

public class CountingGroupElement implements GroupElement {

    @Represented
    GroupElement wrappedElement;

    public CountingGroupElement(GroupElement elem) {
        wrappedElement = elem;
    }

    public CountingGroupElement(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Group getStructure() {
        return wrappedElement.getStructure();
    }

    @Override
    public GroupElement inv() {
        return new CountingGroupElement(wrappedElement.inv());
    }

    @Override
    public GroupElement op(Element e) throws IllegalArgumentException {
        if (e == null)
            throw new IllegalArgumentException("Argument element is null");
        if (e.getClass() != this.getClass())
            throw new IllegalArgumentException("Argument element is not a CountingGroupElement");
        return new CountingGroupElement(wrappedElement.op(((CountingGroupElement) e).wrappedElement));
    }

    @Override
    public GroupElement pow(BigInteger exponent) {
        return new CountingGroupElement(wrappedElement.pow(exponent));
    }

    @Override
    public GroupElement precomputePow() {
        return new CountingGroupElement(wrappedElement.precomputePow());
    }

    @Override
    public GroupElement compute() {
        // counting requires synchronization so we always do computeSync
        return new CountingGroupElement(wrappedElement.computeSync());
    }

    @Override
    public GroupElement computeSync() {
        return new CountingGroupElement(wrappedElement.computeSync());
    }

    @Override
    public boolean isComputed() {
        return wrappedElement.isComputed();
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return wrappedElement.updateAccumulator(accumulator);
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
        return Objects.equals(wrappedElement, that.wrappedElement);
    }

    @Override
    public int hashCode() {
        return wrappedElement.hashCode();
    }
}
