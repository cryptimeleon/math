package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyGroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

public class CountingBilinearMap implements BilinearMap {

    @Represented
    private Zn zn;
    @Represented
    private BigInteger size;
    @Represented
    private BilinearGroup.Type pairingType;
    @Represented
    private PairingExpGroup pairingExpGroup;

    CountingGroup g1, g2, gt;

    public CountingBilinearMap(BilinearGroup.Type type, BigInteger size, PairingExpGroup pairingExpGroup) {
        this.size = size;
        this.zn = new Zn(size);
        this.pairingType = type;
        this.pairingExpGroup = pairingExpGroup;
        init();
    }

    public CountingBilinearMap(Representation repr) {
        ReprUtil.deserialize(this, repr);
        init();
    }

    protected void init() {
        g1 = new CountingGroup("G1", size);
        if (pairingType == BilinearGroup.Type.TYPE_1)
            g2 = g1;
        else
            g2 = new CountingGroup("G2", size);
        gt = new CountingGroup("GT", size);
    }

    @Override
    public Group getG1() {
        return g1;
    }

    @Override
    public Group getG2() {
        return g2;
    }

    @Override
    public Group getGT() {
        return gt;
    }

    @Override
    public GroupElement apply(GroupElement g1, GroupElement g2, BigInteger exponent) {
        if (!(g1 instanceof CountingGroupElement) || !((CountingGroupElement) g1).group.equals(this.g1)) {
            throw new IllegalArgumentException("first pairing argument is not in " + this.g1 + ". It's in "
                    + (!(g1 instanceof CountingGroupElement) ? g1.getStructure() : ((CountingGroupElement) g1).group));
        } else if (!(g2 instanceof CountingGroupElement) || !((CountingGroupElement) g2).group.equals(this.g2)) {
            throw new IllegalArgumentException("first pairing argument is not in " + this.g2 + ". It's in "
                    + (!(g2 instanceof CountingGroupElement) ? g2.getStructure() : ((CountingGroupElement) g2).group));
        }

        // We extract the LazyGroupElement instances so we can do the exponentiation in the selected group.
        // This is done on the LazyGroupElement so it can count the exponentiation as configured, so
        //  either the group operations in the exponentiation algorithm or the exponentiation as a single unit.

        LazyGroupElement g1TotalElement = ((CountingGroupElement) g1).elemTotal;
        LazyGroupElement g1ExpMultiExpElement = ((CountingGroupElement) g1).elemExpMultiExp;

        LazyGroupElement g2TotalElement = ((CountingGroupElement) g2).elemTotal;
        LazyGroupElement g2ExpMultiExpElement = ((CountingGroupElement) g2).elemExpMultiExp;

        DebugGroupElementImpl g1DebugTotalElement;
        DebugGroupElementImpl g2DebugTotalElement;

        // TODO: So we do all computations before applying pairing.
        //  This does not do what the LazyGroup would usually do though, it could still optimize after the pairing
        //  has been "applied" since it is only actually computed once the element is required.

        if (pairingExpGroup == PairingExpGroup.G1) {
            g1DebugTotalElement = (DebugGroupElementImpl)
                    ((LazyGroupElement) g1TotalElement.pow(exponent)).getConcreteGroupElement();
            g2DebugTotalElement = (DebugGroupElementImpl) g2TotalElement.getConcreteGroupElement();

            // don't need result, only for counting the operation
            g1ExpMultiExpElement.pow(exponent).computeSync();
            g2ExpMultiExpElement.computeSync();
        } else if (pairingExpGroup == PairingExpGroup.G2) {
            g1DebugTotalElement = (DebugGroupElementImpl) g1TotalElement.getConcreteGroupElement();
            g2DebugTotalElement = (DebugGroupElementImpl)
                    ((LazyGroupElement) g2TotalElement.pow(exponent)).getConcreteGroupElement();

            g1ExpMultiExpElement.computeSync();
            g2ExpMultiExpElement.pow(exponent).computeSync();
        } else {

        }

        // Only need to do it once since both results should be equal
        CountingGroupElement pairResult = ((CountingGroup) getGT()).wrap(g1DebugTotalElement.elem.mul(g2DebugTotalElement.elem));
        if (pairingExpGroup == PairingExpGroup.GT) {
            pairResult = (CountingGroupElement) pairResult.pow(exponent);
        }
        return pairResult;
    }

    @Override
    public boolean isSymmetric() {
        return pairingType == BilinearGroup.Type.TYPE_1;
    }
}
