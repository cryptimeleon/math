package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupProvider;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.random.interfaces.RandomGenerator;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.AnnotatedRepresentationUtil;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.basic.*;
import de.upb.crypto.math.structures.zn.HashIntoZn;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Objects;

import static de.upb.crypto.math.factory.BilinearGroup.Type.*;

/**
 * Creates bilinear groups based on the integer ring modulo n for some number n.
 * The bilinear map (Zn,+) x (Zn,+) -> (Zn,+) is the ring multiplication.
 * <p>
 * This is intentionally not a {@link BilinearGroupProvider}, because the returned group are not secure!
 */
public class DebugBilinearGroup extends BasicBilinearGroup {
    @Represented
    protected Type pairingType;
    @Represented
    protected BigInteger size;
    @Represented
    protected Boolean wantHashes;

    public DebugBilinearGroup(Type pairingType, BigInteger size) {
        this(pairingType, size, false);
    }

    public DebugBilinearGroup(Type pairingType, BigInteger size, boolean wantHashes) {
        this.pairingType = pairingType;
        this.size = size;
        this.wantHashes = wantHashes;
        init();
    }

    protected void init() {
        DebugBilinearMapImpl bilinearMapImpl = new DebugBilinearMapImpl(pairingType, size);
        g1 = new BasicGroup(bilinearMapImpl.g1);
        g2 = new BasicGroup(bilinearMapImpl.g2);
        gt = new BasicGroup(bilinearMapImpl.gt);
        map = new BasicBilinearMap(g1, g2, gt, bilinearMapImpl);
        if (wantHashes) {
            hashIntoG1 = new BasicHashIntoStructure(new HashIntoDebugGroupImpl(bilinearMapImpl.g1), g1);
            hashIntoG2 = new BasicHashIntoStructure(new HashIntoDebugGroupImpl(bilinearMapImpl.g2), g2);
            hashIntoGt = new BasicHashIntoStructure(new HashIntoDebugGroupImpl(bilinearMapImpl.gt), gt);
        }
        if (pairingType == TYPE_1 || pairingType == TYPE_2)
            homG2toG1 = new BasicGroupHomomorphism(gt, new DebugIsomorphismImpl(bilinearMapImpl.g2, bilinearMapImpl.g1));
    }

    public DebugBilinearGroup(Representation repr) {
        ReprUtil.deserialize(this, repr);
        init();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
