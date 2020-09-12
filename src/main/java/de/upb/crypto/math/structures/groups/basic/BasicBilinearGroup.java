package de.upb.crypto.math.structures.groups.basic;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupImpl;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.util.Objects;

public class BasicBilinearGroup implements BilinearGroup {
    @Represented
    protected BilinearGroupImpl impl;

    BasicGroup g1, g2, gt;
    BasicGroupHomomorphism homG2toG1;
    BilinearMap map;
    HashIntoStructure hashIntoG1, hashIntoG2, hashIntoGt;

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
            hashIntoG1 = new BasicHashIntoStructure(impl.getHashIntoG1(), g1);
        } catch (UnsupportedOperationException e) {
            hashIntoG1 = null;
        }

        try {
            hashIntoG2 = new BasicHashIntoStructure(impl.getHashIntoG2(), g2);
        } catch (UnsupportedOperationException e) {
            hashIntoG2 = null;
        }

        try {
            hashIntoGt = new BasicHashIntoStructure(impl.getHashIntoGT(), gt);
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
    public HashIntoStructure getHashIntoG1() throws UnsupportedOperationException {
        if (hashIntoG1 == null)
            throw new UnsupportedOperationException("No hash available");
        return hashIntoG1;
    }

    @Override
    public HashIntoStructure getHashIntoG2() throws UnsupportedOperationException {
        if (hashIntoG2 == null)
            throw new UnsupportedOperationException("No hash available");
        return hashIntoG2;
    }

    @Override
    public HashIntoStructure getHashIntoGT() throws UnsupportedOperationException {
        if (hashIntoGt == null)
            throw new UnsupportedOperationException("No hash available");
        return hashIntoGt;
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
