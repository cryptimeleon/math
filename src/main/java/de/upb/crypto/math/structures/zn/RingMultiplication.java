package de.upb.crypto.math.structures.zn;

import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingAdditiveGroup;
import de.upb.crypto.math.interfaces.structures.RingAdditiveGroup.RingAdditiveGroupElement;
import de.upb.crypto.math.serialization.RepresentableRepresentation;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

/**
 * The bilinear map G x G -> G, where G is the additive group of a ring.
 * The mapping is simply the ring multiplication, which makes it a bilinear map.
 * If G is an integral domain (e.g., a field), this map is non-degenerate.
 */
public class RingMultiplication implements BilinearMap {
    private Ring ring;

    public RingMultiplication(Representation repr) {
        ring = (Ring) repr.repr().recreateRepresentable();
    }

    public RingMultiplication(Ring ring) {
        this.ring = ring;
    }

    public RingMultiplication(RingAdditiveGroup additiveGroup) {
        this.ring = additiveGroup.getRing();
    }

    @Override
    public RingAdditiveGroupElement apply(GroupElement a, GroupElement b, BigInteger e) {
        return ((RingAdditiveGroup) a.getStructure()).new RingAdditiveGroupElement(((RingAdditiveGroupElement) a.pow(e)).projectToRing().mul(((RingAdditiveGroupElement) b).projectToRing()));
    }

    @Override
    public GroupElement evaluate(PairingProductExpression expr) {
        return expr.stream()
                .map(entry -> (GroupElement) apply(entry.getKey().getG(), entry.getKey().getH(), entry.getValue()))
                .reduce(GroupElement::op)
                .orElse(new RingAdditiveGroup(ring).getNeutralElement());
    }

    @Override
    public Representation getRepresentation() {
        return new RepresentableRepresentation(ring);
    }

    @Override
    public Group getG1() {
        return ring.asAdditiveGroup();
    }

    @Override
    public Group getG2() {
        return getG1();
    }

    @Override
    public Group getGT() {
        return getG1();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ring == null) ? 0 : ring.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RingMultiplication other = (RingMultiplication) obj;
        if (ring == null) {
            if (other.ring != null)
                return false;
        } else if (!ring.equals(other.ring))
            return false;
        return true;
    }

    @Override
    public boolean isSymmetric() {
        return ring.isCommutative();
    }
}
