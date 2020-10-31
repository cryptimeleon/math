package de.upb.crypto.math.structures.groups.basic;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.groups.exp.ExponentiationAlgorithms;
import de.upb.crypto.math.structures.groups.exp.SmallExponentPrecomputation;

import java.math.BigInteger;
import java.util.Objects;

public class BasicGroupElement implements GroupElement {
    protected BasicGroup group;
    protected GroupElementImpl impl;
    protected SmallExponentPrecomputation precomputedSmallExponents;

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
        return new BasicGroupElement(group, ExponentiationAlgorithms.wnafExp(impl, exponent, getPrecomputedSmallExponents(), 4));
    }

    @Override
    public boolean isNeutralElement() {
        return impl.isNeutralElement();
    }

    @Override
    public GroupElement precomputePow() {
        return precomputePow(8);
    }

    @Override
    public GroupElement precomputePow(int windowSize) {
        getPrecomputedSmallExponents().compute(
                windowSize, impl.getStructure().estimateCostInvPerOp() > 1
        );
        return this;
    }

    @Override
    public GroupElement precomputeNegPow() {
        return precomputeNegPow(8);
    }

    @Override
    public GroupElement precomputeNegPow(int windowSize) {
        getPrecomputedSmallExponents().computeNegativePowers(
                windowSize, impl.getStructure().estimateCostInvPerOp() > 1
        );
        return this;
    }

    public SmallExponentPrecomputation getPrecomputedSmallExponents() {
        if (precomputedSmallExponents == null)
            precomputedSmallExponents = new SmallExponentPrecomputation(impl);
        return precomputedSmallExponents;
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
