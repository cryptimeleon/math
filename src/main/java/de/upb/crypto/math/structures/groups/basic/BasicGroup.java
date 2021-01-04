package de.upb.crypto.math.structures.groups.basic;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.serialization.RepresentableRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

/**
 * A basic {@link GroupImpl} wrapper where operations are evaluated naively, i.e. operation by operation.
 * <p>
 * Useful for groups with very efficient operations.
 */
public class BasicGroup implements Group {
    protected GroupImpl impl;
    protected BigInteger size;
    protected boolean isPrimeOrder;
    protected Zn zn;

    public BasicGroup(GroupImpl impl) {
        this.impl = impl;
        try {
            size = impl.size();
            isPrimeOrder = size.isProbablePrime(100);
            zn = isPrimeOrder ? new Zp(size) : new Zn(size);
        } catch (UnsupportedOperationException e) {
            size = null;
            isPrimeOrder = false;
            zn = null;
        }
    }

    public BasicGroup(Representation repr) {
        this((GroupImpl) repr.repr().recreateRepresentable());
    }

    protected BasicGroupElement wrap(GroupElementImpl impl) {
        return new BasicGroupElement(this, impl);
    }

    @Override
    public GroupElement getNeutralElement() {
        return wrap(impl.getNeutralElement());
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return size == null ? impl.size() : size; //handles case where size() throws exception and where size == null
    }

    @Override
    public boolean hasPrimeSize() {
        return isPrimeOrder;
    }

    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return wrap(impl.getUniformlyRandomElement());
    }

    @Override
    public GroupElement getUniformlyRandomNonNeutral() {
        return wrap(impl.getUniformlyRandomNonNeutral());
    }

    @Override
    public GroupElement getElement(Representation repr) {
        return wrap(impl.getElement(repr));
    }

    @Override
    public GroupElement getGenerator() throws UnsupportedOperationException {
        return wrap(impl.getGenerator());
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return impl.getUniqueByteLength();
    }

    @Override
    public boolean isCommutative() {
        return impl.isCommutative();
    }

    @Override
    public Zn getZn() {
        return zn;
    }

    @Override
    public Representation getRepresentation() {
        return new RepresentableRepresentation(impl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicGroup that = (BasicGroup) o;
        return Objects.equals(impl, that.impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }
}
