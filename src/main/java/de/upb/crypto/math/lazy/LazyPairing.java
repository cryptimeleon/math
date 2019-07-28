package de.upb.crypto.math.lazy;

import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.RepresentableRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

public class LazyPairing implements BilinearMap {
    protected LazyGroup g1, g2, gt;
    protected BilinearMap baseBilinearMap;

    public LazyPairing(BilinearMap bilinearMap) {
        this.baseBilinearMap = bilinearMap;
        init();
    }

    public LazyPairing(Representation repr) {
        this.baseBilinearMap = (BilinearMap) repr.repr().recreateRepresentable();
        init();
    }

    private void init() {
        g1 = new LazyGroup(baseBilinearMap.getG1());
        if (baseBilinearMap.isSymmetric()) {
            g2 = g1;
        } else {
            g2 = new LazyGroup(baseBilinearMap.getG2());
        }
        gt = new LazyGroup(baseBilinearMap.getGT(), this);
    }

    @Override
    public LazyGroup getG1() {
        return g1;
    }

    @Override
    public LazyGroup getG2() {
        return g2;
    }

    @Override
    public LazyGroup getGT() {
        return gt;
    }

    @Override
    public LazyGroupElement apply(GroupElement g1, GroupElement g2, BigInteger exponent) {
        return apply(g1, g2).pow(exponent);
    }

    @Override
    public LazyGroupElement apply(GroupElement g1, GroupElement g2) {
        return new LazyGroupElement(gt, baseBilinearMap.expr(((LazyGroupElement) g1).expr, ((LazyGroupElement) g2).expr));
    }

    @Override
    public LazyGroupElement apply(GroupElement g1, GroupElement g2, Zn.ZnElement exponent) {
        return apply(g1, g2, exponent.getInteger());
    }

    @Override
    public boolean isSymmetric() {
        return baseBilinearMap.isSymmetric();
    }

    @Override
    public Representation getRepresentation() {
        return new RepresentableRepresentation(baseBilinearMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LazyPairing that = (LazyPairing) o;
        return Objects.equals(baseBilinearMap, that.baseBilinearMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseBilinearMap);
    }

    @Override
    public String toString() {
        return "lazy " + baseBilinearMap.toString();
    }
}
