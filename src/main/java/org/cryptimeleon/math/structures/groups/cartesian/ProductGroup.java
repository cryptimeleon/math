package org.cryptimeleon.math.structures.groups.cartesian;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

public class ProductGroup implements Group {
    @Represented
    protected Group[] groups;

    public ProductGroup(Group... groups) {
        this.groups = groups;
    }

    public ProductGroup(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public GroupElement getNeutralElement() {
        return new ProductGroupElement(Arrays.stream(groups).map(Group::getNeutralElement).toArray(GroupElement[]::new));
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return Arrays.stream(groups).map(Group::size).reduce(BigInteger.ZERO, (s, s2) -> s == null || s2 == null ? null : s.multiply(s2));
    }

    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return new ProductGroupElement(Arrays.stream(groups).map(Group::getUniformlyRandomElement).toArray(GroupElement[]::new));
    }

    @Override
    public GroupElement restoreElement(Representation repr) {
        return new ProductGroupElement(repr);
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        Optional<Integer> result = Optional.of(0);
        for (Group group : groups) {
            Optional<Integer> ubl = group.getUniqueByteLength();
            if (ubl.isPresent())
                result.map(s -> s + ubl.get());
            else
                result = Optional.empty();
        }

        return result;
    }

    @Override
    public boolean isCommutative() {
        return Arrays.stream(groups).allMatch(Group::isCommutative);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public static ProductGroupElement valueOf(GroupElement... elems) {
        return new ProductGroupElement(elems);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ProductGroup)) {
            return false;
        }
        ProductGroup otherGroup = (ProductGroup) other;
        if (this.groups.length != otherGroup.groups.length) {
            return false;
        }
        for (int i = 0; i < this.groups.length; ++i) {
            if (!this.groups[i].equals(otherGroup.groups[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.groups);
    }
}
