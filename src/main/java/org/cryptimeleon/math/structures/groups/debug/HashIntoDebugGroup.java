package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.HashIntoGroup;
import org.cryptimeleon.math.structures.groups.lazy.HashIntoLazyGroup;

import java.util.Objects;

/**
 * Allows hashing a byte array to {@link DebugGroup}.
 */
public class HashIntoDebugGroup implements HashIntoGroup {

    @Represented
    private HashIntoLazyGroup totalHashIntoGroup;
    @Represented
    private HashIntoLazyGroup expMultiExpHashIntoGroup;

    public HashIntoDebugGroup(HashIntoLazyGroup totalHashIntoStructure,
                              HashIntoLazyGroup expMultiExpHashIntoStructure) {
        this.totalHashIntoGroup = totalHashIntoStructure;
        this.expMultiExpHashIntoGroup = expMultiExpHashIntoStructure;
    }

    public HashIntoDebugGroup(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public GroupElement hash(byte[] x) {
        return new DebugGroupElement(
                new DebugGroup(totalHashIntoGroup.getTarget(), expMultiExpHashIntoGroup.getTarget()),
                totalHashIntoGroup.hash(x),
                expMultiExpHashIntoGroup.hash(x)
        );
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        HashIntoDebugGroup that = (HashIntoDebugGroup) other;
        return Objects.equals(totalHashIntoGroup, that.totalHashIntoGroup)
                && Objects.equals(expMultiExpHashIntoGroup, that.expMultiExpHashIntoGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalHashIntoGroup, expMultiExpHashIntoGroup);
    }
}
