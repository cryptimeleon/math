package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupImpl;
import de.upb.crypto.math.factory.BilinearGroupProvider;
import de.upb.crypto.math.interfaces.mappings.impl.BilinearMapImpl;
import de.upb.crypto.math.interfaces.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.interfaces.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.basic.*;

import java.math.BigInteger;
import java.util.Objects;

import static de.upb.crypto.math.factory.BilinearGroup.Type.*;

/**
 * Creates bilinear groups based on the integer ring modulo n for some number n.
 * The bilinear map \(e : (\mathbb{Z}_n,+) \times (\mathbb{Z}_n,+) \rightarrow (\mathbb{Z}_n,+) is given by
 * the ring multiplication.
 * <p>
 * This is intentionally not a {@link BilinearGroupProvider}, because the returned group is not secure!
 */
public class DebugBilinearGroupImpl implements BilinearGroupImpl {
    @Represented
    protected BilinearGroup.Type pairingType;
    @Represented
    protected BigInteger size;
    @Represented
    protected Boolean wantHashes;
    @Represented
    protected Boolean enableExpCounting;
    @Represented
    protected Boolean enableMultiExpCounting;

    DebugBilinearMapImpl bilinearMapImpl;

    public DebugBilinearGroupImpl(BilinearGroup.Type pairingType, BigInteger size) {
        this(pairingType, size, false, false, false);
    }

    public DebugBilinearGroupImpl(BilinearGroup.Type pairingType, BigInteger size, boolean wantHashes) {
        this(pairingType, size, wantHashes, false, false);
    }

    public DebugBilinearGroupImpl(BilinearGroup.Type pairingType, BigInteger size, boolean wantHashes, boolean enableExpCounting, boolean enableMultiExpCounting) {
        this.pairingType = pairingType;
        this.size = size;
        this.wantHashes = wantHashes;
        this.enableExpCounting = enableExpCounting;
        this.enableMultiExpCounting = enableMultiExpCounting;
        init();
    }

    protected void init() {
        bilinearMapImpl = new DebugBilinearMapImpl(pairingType, size, enableExpCounting, enableMultiExpCounting);
    }

    public DebugBilinearGroupImpl(Representation repr) {
        ReprUtil.deserialize(this, repr);
        init();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        DebugBilinearGroupImpl that = (DebugBilinearGroupImpl) other;
        return Objects.equals(pairingType, that.pairingType)
                && Objects.equals(size, that.size)
                && Objects.equals(wantHashes, that.wantHashes)
                && Objects.equals(enableExpCounting, that.enableExpCounting)
                && Objects.equals(enableMultiExpCounting, that.enableMultiExpCounting);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pairingType.ordinal(), size, wantHashes);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public DebugGroupImpl getG1() {
        return bilinearMapImpl.g1;
    }

    @Override
    public DebugGroupImpl getG2() {
        return bilinearMapImpl.g2;
    }

    @Override
    public DebugGroupImpl getGT() {
        return bilinearMapImpl.gt;
    }

    @Override
    public BilinearMapImpl getBilinearMap() {
        return bilinearMapImpl;
    }

    @Override
    public GroupHomomorphismImpl getHomomorphismG2toG1() throws UnsupportedOperationException {
        if (pairingType != TYPE_1 && pairingType != TYPE_2)
            throw new UnsupportedOperationException("Didn't require existence of a group homomorphism");
        return new DebugIsomorphismImpl(getG2(), getG1());
    }

    @Override
    public HashIntoGroupImpl getHashIntoG1() throws UnsupportedOperationException {
        if (!wantHashes)
            throw new UnsupportedOperationException("Didn't require existence of hashes");
        return new HashIntoDebugGroupImpl(getG1());
    }

    @Override
    public HashIntoGroupImpl getHashIntoG2() throws UnsupportedOperationException {
        if (!wantHashes)
            throw new UnsupportedOperationException("Didn't require existence of hashes");
        return new HashIntoDebugGroupImpl(getG2());
    }

    @Override
    public HashIntoGroupImpl getHashIntoGT() throws UnsupportedOperationException {
        if (!wantHashes)
            throw new UnsupportedOperationException("Didn't require existence of hashes");
        return new HashIntoDebugGroupImpl(getGT());
    }
}
