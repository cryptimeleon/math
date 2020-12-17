package de.upb.crypto.math.structures.groups.basic;

import de.upb.crypto.math.pairings.generic.BilinearGroup;
import de.upb.crypto.math.pairings.generic.BilinearMap;
import de.upb.crypto.math.pairings.generic.BilinearMapImpl;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;

public class BasicBilinearMap implements BilinearMap {
    protected BasicGroup g1, g2, gt;
    protected BilinearMapImpl impl;

    public BasicBilinearMap(BasicGroup g1, BasicGroup g2, BasicGroup gt, BilinearMapImpl impl) {
        this.g1 = g1;
        this.g2 = g2;
        this.gt = gt;
        this.impl = impl;
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
    public BasicGroupElement apply(GroupElement g1, GroupElement g2, BigInteger exponent) {
        return gt.wrap(impl.apply(((BasicGroupElement) g1).impl, ((BasicGroupElement) g2).impl, exponent));
    }

    @Override
    public boolean isSymmetric() {
        return impl.isSymmetric();
    }

    @Override
    public String toString() {
        return "BasicBilinearMap{" +
                "impl=" + impl +
                '}';
    }
}
