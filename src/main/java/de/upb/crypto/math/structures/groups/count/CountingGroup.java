package de.upb.crypto.math.structures.groups.count;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.math.BigInteger;
import java.util.Optional;

public class CountingGroup implements Group {

    @Represented
    Group wrappedGroup;

    public CountingGroup(Group group) {
        wrappedGroup = group;
    }

    public CountingGroup(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public GroupElement getNeutralElement() {
        return new CountingGroupElement(wrappedGroup.getNeutralElement());
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return wrappedGroup.size();
    }

    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return new CountingGroupElement(wrappedGroup.getUniformlyRandomElement());
    }

    @Override
    public GroupElement getElement(Representation repr) {
        return new CountingGroupElement(repr);
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return wrappedGroup.getUniqueByteLength(); // TODO: Is this correct?
    }

    @Override
    public boolean isCommutative() {
        return wrappedGroup.isCommutative();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
