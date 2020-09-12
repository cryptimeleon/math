package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.factory.BilinearGroupImpl;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.IdentityIsomorphism;
import de.upb.crypto.math.interfaces.mappings.impl.BilinearMapImpl;
import de.upb.crypto.math.interfaces.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.AnnotatedRepresentationUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearMap;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyHashIntoStructure;
import de.upb.crypto.math.structures.zn.HashIntoZn;

import java.util.Objects;

public class SupersingularTateGroupImpl implements BilinearGroupImpl {

    @Represented
    private SupersingularSourceGroupImpl g1;
    @Represented
    private SupersingularTargetGroupImpl gt;
    private SupersingularTatePairing pairing;
    @Represented
    private SupersingularSourceHash hashIntoG1;

    public SupersingularTateGroupImpl(SupersingularSourceGroupImpl g1, SupersingularTargetGroupImpl gt, SupersingularTatePairing pairing, SupersingularSourceHash hashIntoG1) {
        this.g1 = g1;
        this.gt = gt;
        this.pairing = pairing;
        this.hashIntoG1 = hashIntoG1;
    }

    public SupersingularTateGroupImpl(Representation repr) {
        AnnotatedRepresentationUtil.restoreAnnotatedRepresentation(repr, this);
        pairing = new SupersingularTatePairing(g1, gt);
    }

    @Override
    public GroupImpl getG1() {
        return g1;
    }

    @Override
    public GroupImpl getG2() {
        return getG1();
    }

    @Override
    public GroupImpl getGT() {
        return gt;
    }

    @Override
    public BilinearMapImpl getBilinearMap() {
        return pairing;
    }

    @Override
    public IdentityIsomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        return new IdentityIsomorphism();
    }

    @Override
    public HashIntoGroupImpl getHashIntoG1() throws UnsupportedOperationException {
        return hashIntoG1;
    }

    @Override
    public HashIntoGroupImpl getHashIntoG2() throws UnsupportedOperationException {
        return getHashIntoG1();
    }

    @Override
    public HashIntoGroupImpl getHashIntoGT() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The hash function into the target group is not implemented yet!");
    }

    @Override
    public Representation getRepresentation() {
        return AnnotatedRepresentationUtil.putAnnotatedRepresentation(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupersingularTateGroupImpl that = (SupersingularTateGroupImpl) o;
        return g1.equals(that.g1) &&
                gt.equals(that.gt) &&
                pairing.equals(that.pairing) &&
                hashIntoG1.equals(that.hashIntoG1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(g1);
    }
}
