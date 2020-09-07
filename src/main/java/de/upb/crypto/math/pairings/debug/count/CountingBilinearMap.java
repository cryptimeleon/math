package de.upb.crypto.math.pairings.debug.count;

import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.debug.DebugBilinearMapImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearMap;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyGroupElement;

import java.math.BigInteger;
import java.util.Objects;

public class CountingBilinearMap implements BilinearMap {

    @Represented
    LazyBilinearMap totalBilMap;
    @Represented
    LazyBilinearMap expMultiExpBilMap;

    public CountingBilinearMap(LazyBilinearMap totalBilMap, LazyBilinearMap expMultiExpBilMap) {
        this.totalBilMap = totalBilMap;
        this.expMultiExpBilMap = expMultiExpBilMap;
    }

    public CountingBilinearMap(Representation repr) {
        ReprUtil.deserialize(this, repr);
    }

    @Override
    public Group getG1() {
        return new CountingGroup((LazyGroup) totalBilMap.getG1(), (LazyGroup) expMultiExpBilMap.getG1());
    }

    @Override
    public Group getG2() {
        return new CountingGroup((LazyGroup) totalBilMap.getG2(), (LazyGroup) expMultiExpBilMap.getG2());
    }

    @Override
    public Group getGT() {
        return new CountingGroup((LazyGroup) totalBilMap.getGT(), (LazyGroup) expMultiExpBilMap.getGT());
    }

    @Override
    public GroupElement apply(GroupElement g1, GroupElement g2, BigInteger exponent) {
        CountingGroupElement g1Cast = (CountingGroupElement) g1;
        CountingGroupElement g2Cast = (CountingGroupElement) g2;
        LazyGroupElement g1Result = (LazyGroupElement) totalBilMap.apply(g1Cast.elemTotal, g2Cast.elemTotal, exponent);
        LazyGroupElement g2Result =
                (LazyGroupElement) expMultiExpBilMap.apply(g1Cast.elemExpMultiExp, g2Cast.elemExpMultiExp, exponent);
        return new CountingGroupElement(
                (CountingGroup) getGT(),
                g1Result,
                g2Result
        );
    }

    @Override
    public boolean isSymmetric() {
        return totalBilMap.isSymmetric() && expMultiExpBilMap.isSymmetric();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        CountingBilinearMap that = (CountingBilinearMap) other;
        return Objects.equals(totalBilMap, that.totalBilMap)
                && Objects.equals(expMultiExpBilMap, that.expMultiExpBilMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalBilMap, expMultiExpBilMap);
    }

    /**
     * Retrieves number of pairings computed in this bilinear group.
     */
    public long getNumPairings() {
        // one of them suffices since both count
        return ((DebugBilinearMapImpl) totalBilMap.getImpl()).getNumPairings();
    }

    /**
     * Resets pairing counter.
     */
    public void resetNumPairings() {
        ((DebugBilinearMapImpl) totalBilMap.getImpl()).resetNumPairings();
        ((DebugBilinearMapImpl) expMultiExpBilMap.getImpl()).resetNumPairings();
    }

    @Override
    public String toString() {
        if (isSymmetric()) {
            return "Symmetric CountingBilinearMap(" + totalBilMap + ";" + expMultiExpBilMap + ")";
        } else {
            return "Asymmetric CountingBilinearMap(" + totalBilMap + ";" + expMultiExpBilMap + ")";
        }
    }

    public String formatCounterData()  {
        return "---------- Operation data for " + toString() + "----------\n"
                + ((CountingGroup) getG1()).formatCounterData()
                + ((isSymmetric()) ? "" : ((CountingGroup) getG2()).formatCounterData())
                + ((CountingGroup) getGT()).formatCounterData()
                + "------- Number of pairings: " + getNumPairings() + " -------";
    }
}
