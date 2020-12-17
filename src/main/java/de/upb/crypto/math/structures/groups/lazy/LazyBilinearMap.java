package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.pairings.generic.BilinearGroup;
import de.upb.crypto.math.pairings.generic.BilinearMap;
import de.upb.crypto.math.pairings.generic.BilinearMapImpl;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;
import java.util.Objects;

public class LazyBilinearMap implements BilinearMap {
    protected BilinearMapImpl impl;
    protected LazyGroup g1, g2, gt;

    public LazyBilinearMap(LazyGroup g1, LazyGroup g2, LazyGroup gt, BilinearMapImpl impl) {
        this.impl = impl;
        this.g1 = g1;
        this.g2 = g2;
        this.gt = gt;
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
        return new PairingResultLazyGroupElement(gt, this, g1, g2).pow(exponent);
    }

    @Override
    public GroupElement apply(GroupElement g1, GroupElement g2) {
        return new PairingResultLazyGroupElement(gt, this, g1, g2);
    }

    @Override
    public boolean isSymmetric() {
        return impl.isSymmetric();
    }

    @Override
    public String toString() {
        return impl.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LazyBilinearMap that = (LazyBilinearMap) o;
        return impl.equals(that.impl) &&
                g1.equals(that.g1) &&
                g2.equals(that.g2) &&
                gt.equals(that.gt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }

    public BilinearMapImpl getImpl() {
        return impl;
    }
}
