package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.IdentityIsomorphism;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.AnnotatedRepresentationUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import de.upb.crypto.math.structures.zn.HashIntoZn;

public class SupersingularTateGroup implements BilinearGroup {

    @Represented
    private SupersingularSourceGroup g1;
    @Represented
    private SupersingularTargetGroup gt;
    @Represented
    private SupersingularTatePairing pairing;
    @Represented
    private SupersingularSourceHash hashIntoG1;

    public SupersingularTateGroup(SupersingularSourceGroup g1, SupersingularTargetGroup gt, SupersingularTatePairing pairing, SupersingularSourceHash hashIntoG1) {
        this.g1 = g1;
        this.gt = gt;
        this.pairing = pairing;
        this.hashIntoG1 = hashIntoG1;
    }

    public SupersingularTateGroup(Representation repr) {
        AnnotatedRepresentationUtil.restoreAnnotatedRepresentation(repr, this);
    }

    @Override
    public SupersingularSourceGroup getG1() {
        return g1;
    }

    @Override
    public SupersingularSourceGroup getG2() {
        return g1;
    }

    @Override
    public SupersingularTargetGroup getGT() {
        return gt;
    }

    @Override
    public SupersingularTatePairing getBilinearMap() {
        return pairing;
    }

    @Override
    public IdentityIsomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        return new IdentityIsomorphism();
    }

    @Override
    public SupersingularSourceHash getHashIntoG1() throws UnsupportedOperationException {
        return hashIntoG1;
    }

    @Override
    public SupersingularSourceHash getHashIntoG2() throws UnsupportedOperationException {
        return hashIntoG1;
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
