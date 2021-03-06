package org.cryptimeleon.math.structures.groups.basic;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.HashIntoGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMap;
import org.cryptimeleon.math.structures.groups.mappings.GroupHomomorphism;

import java.util.Objects;

/**
 * A basic {@link BilinearGroupImpl} wrapper where operations are evaluated naively, i.e. operation by operation.
 */
public class BasicBilinearGroup implements BilinearGroup {
    @Represented
    protected BilinearGroupImpl impl;

    BasicGroup g1, g2, gt;
    BasicGroupHomomorphism homG2toG1;
    BilinearMap map;
    HashIntoGroup hashIntoG1, hashIntoG2, hashIntoGt;

    public BasicBilinearGroup(BilinearGroupImpl impl) {
        this.impl = impl;
        instantiateBasicStuff();
    }

    public BasicBilinearGroup(Representation repr) {
        ReprUtil.deserialize(this, repr);
        instantiateBasicStuff();
    }

    protected void instantiateBasicStuff() {
        g1 = new BasicGroup(impl.getG1());
        g2 = new BasicGroup(impl.getG2());
        gt = new BasicGroup(impl.getGT());

        map = new BasicBilinearMap(g1, g2, gt, impl.getBilinearMap());
        try {
            homG2toG1 = new BasicGroupHomomorphism(g1, impl.getHomomorphismG2toG1());
        } catch (UnsupportedOperationException e) {
            homG2toG1 = null;
        }

        try {
            hashIntoG1 = new HashIntoBasicGroup(impl.getHashIntoG1(), g1);
        } catch (UnsupportedOperationException e) {
            hashIntoG1 = null;
        }

        try {
            hashIntoG2 = new HashIntoBasicGroup(impl.getHashIntoG2(), g2);
        } catch (UnsupportedOperationException e) {
            hashIntoG2 = null;
        }

        try {
            hashIntoGt = new HashIntoBasicGroup(impl.getHashIntoGT(), gt);
        } catch (UnsupportedOperationException e) {
            hashIntoGt = null;
        }
    }

    @Override
    public BasicGroup getG1() {
        return g1;
    }

    @Override
    public BasicGroup getG2() {
        return g2;
    }

    @Override
    public BasicGroup getGT() {
        return gt;
    }

    @Override
    public BilinearMap getBilinearMap() {
        return map;
    }

    @Override
    public GroupHomomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        if (homG2toG1 == null)
            throw new UnsupportedOperationException("No homomorphism available");
        return homG2toG1;
    }

    @Override
    public HashIntoGroup getHashIntoG1() throws UnsupportedOperationException {
        if (hashIntoG1 == null)
            throw new UnsupportedOperationException("No hash available");
        return hashIntoG1;
    }

    @Override
    public HashIntoGroup getHashIntoG2() throws UnsupportedOperationException {
        if (hashIntoG2 == null)
            throw new UnsupportedOperationException("No hash available");
        return hashIntoG2;
    }

    @Override
    public HashIntoGroup getHashIntoGT() throws UnsupportedOperationException {
        if (hashIntoGt == null)
            throw new UnsupportedOperationException("No hash available");
        return hashIntoGt;
    }

    @Override
    public Integer getSecurityLevel() {
        return impl.getSecurityLevel();
    }

    @Override
    public Type getPairingType() {
        return impl.getPairingType();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicBilinearGroup that = (BasicBilinearGroup) o;
        return impl.equals(that.impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }
}
