package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMap;
import org.cryptimeleon.math.structures.groups.lazy.LazyBilinearMap;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroup;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

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
 * {@link DebugBilinearGroupImpl}s themselves. All operations are executed in both groups,
 * one counts total group operations and one counts each (multi-)exponentiation as one unit.
 * This allows for tracking both kinds of data.
 *
 * @see DebugBilinearGroup
 */
public class DebugBilinearMap implements BilinearMap {

    @Represented
    LazyBilinearMap totalBilMap;
    @Represented
    LazyBilinearMap expMultiExpBilMap;

    public DebugBilinearMap(LazyBilinearMap totalBilMap, LazyBilinearMap expMultiExpBilMap) {
        this.totalBilMap = totalBilMap;
        this.expMultiExpBilMap = expMultiExpBilMap;
    }

    public DebugBilinearMap(Representation repr) {
        ReprUtil.deserialize(this, repr);
    }

    @Override
    public Group getG1() {
        return new DebugGroup((LazyGroup) totalBilMap.getG1(), (LazyGroup) expMultiExpBilMap.getG1());
    }

    @Override
    public Group getG2() {
        return new DebugGroup((LazyGroup) totalBilMap.getG2(), (LazyGroup) expMultiExpBilMap.getG2());
    }

    @Override
    public Group getGT() {
        return new DebugGroup((LazyGroup) totalBilMap.getGT(), (LazyGroup) expMultiExpBilMap.getGT());
    }

    @Override
    public GroupElement apply(GroupElement g1, GroupElement g2, BigInteger exponent) {
        DebugGroupElement g1Cast = (DebugGroupElement) g1;
        DebugGroupElement g2Cast = (DebugGroupElement) g2;
        LazyGroupElement g1Result = (LazyGroupElement) totalBilMap.apply(g1Cast.elemTotal, g2Cast.elemTotal, exponent);
        LazyGroupElement g2Result =
                (LazyGroupElement) expMultiExpBilMap.apply(g1Cast.elemExpMultiExp, g2Cast.elemExpMultiExp, exponent);
        return new DebugGroupElement(
                (DebugGroup) getGT(),
                g1Result,
                g2Result
        );
    }

    @Override
    public GroupElement apply(GroupElement g1, GroupElement g2) {
        // We overwrite this to prevent the default method from BilinearMap to be used which would
        // execute an exponentiation with exponent one. This introduces an unnecessary exponentiation into the counting.
        DebugGroupElement g1Cast = (DebugGroupElement) g1;
        DebugGroupElement g2Cast = (DebugGroupElement) g2;
        LazyGroupElement g1Result = (LazyGroupElement) totalBilMap.apply(g1Cast.elemTotal, g2Cast.elemTotal);
        LazyGroupElement g2Result =
                (LazyGroupElement) expMultiExpBilMap.apply(g1Cast.elemExpMultiExp, g2Cast.elemExpMultiExp);
        return new DebugGroupElement(
                (DebugGroup) getGT(),
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
        DebugBilinearMap that = (DebugBilinearMap) other;
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
                + ((DebugGroup) getG1()).formatCounterData()
                + ((isSymmetric()) ? "" : ((DebugGroup) getG2()).formatCounterData())
                + ((DebugGroup) getGT()).formatCounterData()
                + "------- Number of pairings: " + getNumPairings() + " -------";
    }
}
