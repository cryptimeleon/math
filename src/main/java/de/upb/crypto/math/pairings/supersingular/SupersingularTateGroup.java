package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.IdentityIsomorphism;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.AnnotatedRepresentationUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearMap;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyHashIntoStructure;
import de.upb.crypto.math.structures.zn.HashIntoZn;

public class SupersingularTateGroup implements BilinearGroup {

    @Represented
    private SupersingularSourceGroupImpl g1;
    @Represented
    private SupersingularTargetGroupImpl gt;
    private SupersingularTatePairing pairing;
    @Represented
    private SupersingularSourceHash hashIntoG1;

    public SupersingularTateGroup(SupersingularSourceGroupImpl g1, SupersingularTargetGroupImpl gt, SupersingularTatePairing pairing, SupersingularSourceHash hashIntoG1) {
        this.g1 = g1;
        this.gt = gt;
        this.pairing = pairing;
        this.hashIntoG1 = hashIntoG1;
    }

    public SupersingularTateGroup(Representation repr) {
        AnnotatedRepresentationUtil.restoreAnnotatedRepresentation(repr, this);
        pairing = new SupersingularTatePairing(g1, gt);
    }

    @Override
    public LazyGroup getG1() {
        return new LazyGroup(g1);
    }

    @Override
    public LazyGroup getG2() {
        return getG1();
    }

    @Override
    public LazyGroup getGT() {
        return new LazyGroup(gt);
    }

    @Override
    public BilinearMap getBilinearMap() {
        return new LazyBilinearMap(pairing, getG1(), getG2(), getGT());
    }

    @Override
    public IdentityIsomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        return new IdentityIsomorphism();
    }

    @Override
    public LazyHashIntoStructure getHashIntoG1() throws UnsupportedOperationException {
        return new LazyHashIntoStructure(hashIntoG1, (LazyGroup) getG1());
    }

    @Override
    public LazyHashIntoStructure getHashIntoG2() throws UnsupportedOperationException {
        return getHashIntoG1();
    }

    @Override
    public HashIntoStructure getHashIntoGT() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The hash function into the target group is not implemented yet!");
    }

    @Override
    public HashIntoStructure getHashIntoZGroupExponent() throws UnsupportedOperationException {
        return new HashIntoZn(this.getG1().size());
    }


    @Override
    public Representation getRepresentation() {
        return AnnotatedRepresentationUtil.putAnnotatedRepresentation(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SupersingularTateGroup that = (SupersingularTateGroup) o;

        if (g1 != null ? !g1.equals(that.g1) : that.g1 != null) return false;
        if (gt != null ? !gt.equals(that.gt) : that.gt != null) return false;
        if (pairing != null ? !pairing.equals(that.pairing) : that.pairing != null) return false;
        return hashIntoG1 != null ? hashIntoG1.equals(that.hashIntoG1) : that.hashIntoG1 == null;
    }

    @Override
    public int hashCode() {
        int result = g1 != null ? g1.hashCode() : 0;
        result = 31 * result + (gt != null ? gt.hashCode() : 0);
        result = 31 * result + (pairing != null ? pairing.hashCode() : 0);
        result = 31 * result + (hashIntoG1 != null ? hashIntoG1.hashCode() : 0);
        return result;
    }
}
