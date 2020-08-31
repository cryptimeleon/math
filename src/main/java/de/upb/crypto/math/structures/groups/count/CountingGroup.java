package de.upb.crypto.math.structures.groups.count;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.debug.DebugGroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyGroupElement;

import java.math.BigInteger;
import java.util.Optional;

public class CountingGroup implements Group {

    @Represented
    LazyGroup groupTotal;

    @Represented
    LazyGroup groupExpMultiExp;

    public CountingGroup(String name, BigInteger n) {
        groupTotal = new LazyGroup(new DebugGroupImpl(name, n, false, false));
        groupExpMultiExp = new LazyGroup(new DebugGroupImpl(name, n, true, true));
    }

    public CountingGroup(LazyGroup groupTotal, LazyGroup groupExpMultiExp) {
        this.groupTotal = groupTotal;
        this.groupExpMultiExp = groupExpMultiExp;
    }

    public CountingGroup(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public GroupElement getNeutralElement() {
        return new CountingGroupElement(
                (LazyGroupElement) groupTotal.getNeutralElement(),
                (LazyGroupElement) groupExpMultiExp.getNeutralElement()
        );
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return groupTotal.size();
    }

    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return new CountingGroupElement(
                (LazyGroupElement) groupTotal.getUniformlyRandomElement(),
                (LazyGroupElement) groupExpMultiExp.getUniformlyRandomElement()
        );
    }

    @Override
    public GroupElement getElement(Representation repr) {
        return new CountingGroupElement(repr);
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        // TODO: Is this correct?
        Optional<Integer> totalLength = groupTotal.getUniqueByteLength();
        Optional<Integer> expMultiExpLength = groupExpMultiExp.getUniqueByteLength();
        if (!totalLength.isPresent() || !expMultiExpLength.isPresent()) {
            return Optional.empty();
        } else {
            return Optional.of(totalLength.get() + expMultiExpLength.get());
        }
    }

    @Override
    public boolean isCommutative() {
        return groupTotal.isCommutative();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
