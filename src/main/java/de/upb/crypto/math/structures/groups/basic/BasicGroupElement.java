package de.upb.crypto.math.structures.groups.basic;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;
import java.util.Objects;

public class BasicGroupElement implements GroupElement {
    protected BasicGroup group;
    protected GroupElementImpl impl;

    public BasicGroupElement(BasicGroup group, GroupElementImpl impl) {
        this.group = group;
        this.impl = impl;
    }

    @Override
    public Group getStructure() {
        return group;
    }

    @Override
    public GroupElement inv() {
        return new BasicGroupElement(group, impl.inv());
    }

    @Override
    public GroupElement op(Element e) throws IllegalArgumentException {
        return new BasicGroupElement(group, impl.op(((BasicGroupElement) e).impl));
    }

    @Override
    public GroupElement square() {
        return new BasicGroupElement(group, impl.square());
    }

    @Override
    public GroupElement pow(BigInteger exponent) {
        return new BasicGroupElement(group, impl.pow(exponent));
    }

    @Override
    public boolean isNeutralElement() {
        return impl.isNeutralElement();
    }

    @Override
    public GroupElement precomputePow() {
        return this;
    }

    @Override
    public GroupElement compute() {
        return this;
    }

    @Override
    public GroupElement computeSync() {
        return this;
    }

    public GroupElementImpl getConcreteGroupElement() {
        return impl;
    }

    @Override
    public boolean isComputed() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicGroupElement that = (BasicGroupElement) o;
        return Objects.equals(impl, that.impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        getConcreteGroupElement().updateAccumulator(accumulator);
        return accumulator;
    }

    @Override
    public Representation getRepresentation() {
        return getConcreteGroupElement().getRepresentation();
    }

    @Override
    public String toString() {
        return impl.toString();
    }
}
