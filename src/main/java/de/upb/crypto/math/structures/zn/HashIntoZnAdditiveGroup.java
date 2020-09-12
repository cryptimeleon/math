package de.upb.crypto.math.structures.zn;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.structures.RingGroup;
import de.upb.crypto.math.interfaces.structures.group.impl.RingAdditiveGroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.RingAdditiveGroupImpl.RingAdditiveGroupElementImpl;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

/**
 * Hashes into the additive subgroup of Zn.
 *
 * @see HashIntoZn
 */
public class HashIntoZnAdditiveGroup implements HashIntoStructure {
    protected HashIntoZn znHash;

    /**
     * target group
     */
    protected RingGroup structure;

    /**
     * Corresponds to new HashIntoZn()
     */
    public HashIntoZnAdditiveGroup(BigInteger n) {
        this(new HashIntoZn(n));
    }

    /**
     * Corresponds to new HashIntoZn(String)
     */
    public HashIntoZnAdditiveGroup(Zn ring) {
        this(new HashIntoZn(ring.n));
    }

    /**
     * Recreate hash function from representation
     */
    public HashIntoZnAdditiveGroup(Representation repr) {
        this(new HashIntoZn(repr));
    }

    public HashIntoZnAdditiveGroup(HashIntoZn hashIntoZn) {
        znHash = hashIntoZn;
        structure = znHash.getTargetStructure().asAdditiveGroup();
    }

    @Override
    public RingGroup.RingGroupElement hashIntoStructure(byte[] x) {
        return structure.getElement(znHash.hashIntoStructure(x));
    }

    @Override
    public Representation getRepresentation() {
        return znHash.getRepresentation();
    }


    @Override
    public int hashCode() {
        return structure.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HashIntoZnAdditiveGroup &&
                //structure.equals(((HashIntoZnAdditiveGroup) obj).structure) &&
                znHash.equals(((HashIntoZnAdditiveGroup) obj).znHash);
    }
}
