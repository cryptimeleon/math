package de.upb.crypto.math.pairings.counting;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyHashIntoStructure;

import java.util.Objects;

/**
 * Allows hashing a byte array to {@link CountingGroup}.
 */
public class CountingHashIntoStructure implements HashIntoStructure {

    @Represented
    private LazyHashIntoStructure totalHashIntoStructure;
    @Represented
    private LazyHashIntoStructure expMultiExpHashIntoStructure;

    public CountingHashIntoStructure(LazyHashIntoStructure totalHashIntoStructure, LazyHashIntoStructure expMultiExpHashIntoStructure) {
        this.totalHashIntoStructure = totalHashIntoStructure;
        this.expMultiExpHashIntoStructure = expMultiExpHashIntoStructure;
    }

    public CountingHashIntoStructure(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Element hashIntoStructure(byte[] x) {
        return new CountingGroupElement(
                new CountingGroup(totalHashIntoStructure.getTarget(), expMultiExpHashIntoStructure.getTarget()),
                totalHashIntoStructure.hashIntoStructure(x),
                expMultiExpHashIntoStructure.hashIntoStructure(x)
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
        CountingHashIntoStructure that = (CountingHashIntoStructure) other;
        return Objects.equals(totalHashIntoStructure, that.totalHashIntoStructure)
                && Objects.equals(expMultiExpHashIntoStructure, that.expMultiExpHashIntoStructure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalHashIntoStructure, expMultiExpHashIntoStructure);
    }
}
