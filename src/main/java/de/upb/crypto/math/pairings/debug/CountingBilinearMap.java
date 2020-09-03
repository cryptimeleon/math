package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
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

        // TODO: Count pairings.
        // TODO: Where to count the exponentiation? This depends on the actual groups.
        //  Let user configure this for now.

        DebugGroupElementImpl g1TotalElement = (DebugGroupElementImpl) ((CountingGroupElement) g1).elemTotal.getConcreteGroupElement();
        DebugGroupElementImpl g1ExpMultiExpElement = (DebugGroupElementImpl) ((CountingGroupElement) g1).elemExpMultiExp.getConcreteGroupElement();

        DebugGroupElementImpl g2TotalElement = (DebugGroupElementImpl) ((CountingGroupElement) g2).elemTotal.getConcreteGroupElement();
        DebugGroupElementImpl g2ExpMultiExpElement = (DebugGroupElementImpl) ((CountingGroupElement) g2).elemExpMultiExp.getConcreteGroupElement();

        if (pairingExpGroup == PairingExpGroup.G1) {
            g1TotalElement = (DebugGroupElementImpl) g1TotalElement.pow(exponent);
            // don't need result, only for counting the operation
            g1ExpMultiExpElement.pow(exponent);
        } else if (pairingExpGroup == PairingExpGroup.G2) {
            g2TotalElement = (DebugGroupElementImpl) g2TotalElement.pow(exponent);
            g2ExpMultiExpElement.pow(exponent);
        }

        // Only need to do it once since both results should be equal
        CountingGroupElement pairResult = ((CountingGroup) getGT()).wrap(g1TotalElement.elem.mul(g2TotalElement.elem));
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
