package de.upb.crypto.math.structures.zn;

import de.upb.crypto.math.interfaces.mappings.impl.BilinearMapImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.group.impl.RingAdditiveGroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.RingAdditiveGroupImpl.RingAdditiveGroupElementImpl;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

/**
 * The bilinear map {@code G x G -> G},
 * where G is the additive group of a ring.
 * <p>
 * The mapping is simply the ring multiplication, which makes it a bilinear map.
 * If G is an integral domain (e.g. a field), this map is non-degenerate.
 */
public class RingMultiplicationImpl implements BilinearMapImpl {
    private final Ring ring;

    public RingMultiplicationImpl(Representation repr) {
        ring = (Ring) repr.repr().recreateRepresentable();
    }

    public RingMultiplicationImpl(Ring ring) {
        this.ring = ring;
    }

    public RingMultiplicationImpl(RingAdditiveGroupImpl additiveGroup) {
        this.ring = additiveGroup.getRing();
    }

    @Override
    public RingAdditiveGroupElementImpl apply(GroupElementImpl a, GroupElementImpl b, BigInteger e) {
        return ((RingAdditiveGroupImpl) a.getStructure()).new RingAdditiveGroupElementImpl(((RingAdditiveGroupElementImpl) a.pow(e)).projectToRing().mul(((RingAdditiveGroupElementImpl) b).projectToRing()));
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
        RingMultiplicationImpl other = (RingMultiplicationImpl) obj;
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
