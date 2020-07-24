package de.upb.crypto.math.structures.groups.basic;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import java.util.Objects;

public abstract class BasicBilinearGroup implements BilinearGroup {
    protected BasicGroup g1, g2, gt;
    protected BasicBilinearMap map;
    protected BasicHashIntoStructure hashIntoG1, hashIntoG2, hashIntoGt;
    protected BasicGroupHomomorphism homG2toG1;

    public BasicBilinearGroup(BasicGroup g1, BasicGroup g2, BasicGroup gt, BasicBilinearMap map, BasicHashIntoStructure hashIntoG1, BasicHashIntoStructure hashIntoG2, BasicHashIntoStructure hashIntoGt, BasicGroupHomomorphism homG2toG1) {
        this.g1 = g1;
        this.g2 = g2;
        this.gt = gt;
        this.map = map;
        this.hashIntoG1 = hashIntoG1;
        this.hashIntoG2 = hashIntoG2;
        this.hashIntoGt = hashIntoGt;
        this.homG2toG1 = homG2toG1;
    }

    public BasicBilinearGroup(Representation repr) {
        ReprUtil.deserialize(this, repr);
        //Subclasses still have to set map
    }

    protected BasicBilinearGroup() {
        //Allows subclasses to skip the constructor and set the values for all the fields themselves.
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
    public BilinearMap getBilinearMap() {
        return map;
    }

    @Override
    public GroupHomomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        return homG2toG1;
    }

    @Override
    public HashIntoStructure getHashIntoG1() throws UnsupportedOperationException {
        return hashIntoG1;
    }

    @Override
    public HashIntoStructure getHashIntoG2() throws UnsupportedOperationException {
        return hashIntoG2;
    }

    @Override
    public HashIntoStructure getHashIntoGT() throws UnsupportedOperationException {
        return hashIntoGt;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        BasicBilinearGroup that = (BasicBilinearGroup) o;
        return Objects.equals(g1, that.g1) &&
                Objects.equals(g2, that.g2) &&
                Objects.equals(gt, that.gt) &&
                Objects.equals(map, that.map) &&
                Objects.equals(hashIntoG1, that.hashIntoG1) &&
                Objects.equals(hashIntoG2, that.hashIntoG2) &&
                Objects.equals(hashIntoGt, that.hashIntoGt) &&
                Objects.equals(homG2toG1, that.homG2toG1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(g1, g2, gt, map, hashIntoG1, hashIntoG2, hashIntoGt, homG2toG1);
    }
}
