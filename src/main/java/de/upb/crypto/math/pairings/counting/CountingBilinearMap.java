package de.upb.crypto.math.pairings.counting;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.BilinearMap;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearMap;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyGroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A {@link BilinearMap} implementing a fast, but insecure pairing over {@link Zn}.
 * Allows for counting group operations and (multi-)exponentiations as well as pairings on the bilinear
 * group level.
 * <p>
 * The bilinear map works by mapping {@code (Zn,+) x (Zn,+)} to {@code (Zn,+)} via {@code (a,b) -> a*b}
 * (multiplication in Zn).
 * It is insecure since DLOG is trivial in Zn.
 * <p>
 * The counting capability is implemented by wrapping two {@link LazyBilinearMap}s which contain
 * {@link CountingBilinearGroupImpl}s themselves. All operations are executed in both groups,
 * one counts total group operations and one counts each (multi-)exponentiation as one unit.
 * This allows for tracking both kinds of data.
 *
 * @see CountingBilinearGroup
 */
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
    public GroupElement apply(GroupElement g1, GroupElement g2) {
        // We overwrite this to prevent the default method from BilinearMap to be used which would
        // execute an exponentiation with exponent one. This introduces an unnecessary exponentiation into the counting.
        CountingGroupElement g1Cast = (CountingGroupElement) g1;
        CountingGroupElement g2Cast = (CountingGroupElement) g2;
        LazyGroupElement g1Result = (LazyGroupElement) totalBilMap.apply(g1Cast.elemTotal, g2Cast.elemTotal);
        LazyGroupElement g2Result =
                (LazyGroupElement) expMultiExpBilMap.apply(g1Cast.elemExpMultiExp, g2Cast.elemExpMultiExp);
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
        return ((CountingBilinearMapImpl) totalBilMap.getImpl()).getNumPairings();
    }

    /**
     * Resets pairing counter.
     */
    public void resetNumPairings() {
        ((CountingBilinearMapImpl) totalBilMap.getImpl()).resetNumPairings();
        ((CountingBilinearMapImpl) expMultiExpBilMap.getImpl()).resetNumPairings();
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
