package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMapImpl;
import org.cryptimeleon.math.structures.groups.mappings.impl.GroupHomomorphismImpl;
import org.cryptimeleon.math.structures.groups.mappings.impl.HashIntoGroupImpl;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A {@link BilinearGroupImpl} implementing a fast, but insecure pairing over {@link Zn}.
 * Allows for selecting between counting total group operations, or (multi-)exponentiations plus group operations
 * outside of (multi-)exponentiations.
 * <p>
 * Usually, you should be using {@link DebugBilinearGroup} instead of this class as it provides pairing counting
 * capabilities and allows for tracking both kinds of group operation data at once.
 * 
 * @see DebugBilinearGroup
 */
public class DebugBilinearGroupImpl implements BilinearGroupImpl {

    /**
     * The security level offered by this bilinear group in number of bits.
     */
    @Represented
    protected Integer securityParameter;

    /**
     * The type of pairing this bilinear group should offer.
     */
    @Represented
    protected BilinearGroup.Type pairingType;

    /**
     * The group order of this bilinear group.
     */
    @Represented
    protected BigInteger size;

    /**
     * Whether to count exponentiations and multi-exponentiations as a single unit.
     * If set to true, group operations in those (multi-)exponentiations will not be counted.
     * Instead (multi-)exponentiations will be counted as a single unit.
     */
    @Represented
    protected Boolean enableExpMultiExpCounting;

    /**
     * The underlying bilinear map used for applying the pairing function and counting it.
     */
    DebugBilinearMapImpl bilinearMapImpl;

    public DebugBilinearGroupImpl(int groupSizeBits, BilinearGroup.Type pairingType,
                                  boolean enableExpMultiExpCounting) {
        this.securityParameter = groupSizeBits;
        this.pairingType = pairingType;
        this.enableExpMultiExpCounting = enableExpMultiExpCounting;

        // get a random prime of roughly groupSizeBits
        this.size = RandomGenerator.getRandomPrime(groupSizeBits);
        init();
    }

    public DebugBilinearGroupImpl(BigInteger groupSize, BilinearGroup.Type pairingType,
                                  boolean enableExpMultiExpCounting) {
        this.securityParameter = groupSize.bitLength();
        this.pairingType = pairingType;
        this.enableExpMultiExpCounting = enableExpMultiExpCounting;
        this.size = groupSize;
        init();
    }

    protected void init() {
        bilinearMapImpl = new DebugBilinearMapImpl(size, pairingType, enableExpMultiExpCounting);
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
                && Objects.equals(enableExpMultiExpCounting, that.enableExpMultiExpCounting);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pairingType.ordinal(), size);
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
        if (pairingType != BilinearGroup.Type.TYPE_1 && pairingType != BilinearGroup.Type.TYPE_2)
            throw new UnsupportedOperationException("Didn't require existence of a group homomorphism");
        return new DebugIsomorphismImpl(getG2(), getG1());
    }

    @Override
    public HashIntoGroupImpl getHashIntoG1() throws UnsupportedOperationException {
        return new HashIntoDebugGroupImpl(getG1());
    }

    @Override
    public HashIntoGroupImpl getHashIntoG2() throws UnsupportedOperationException {
        return new HashIntoDebugGroupImpl(getG2());
    }

    @Override
    public HashIntoGroupImpl getHashIntoGT() throws UnsupportedOperationException {
        return new HashIntoDebugGroupImpl(getGT());
    }

    /**
     * Returns the security level offered by this bilinear group in number of bits.
     */
    @Override
    public Integer getSecurityLevel() {
        return securityParameter;
    }

    /**
     * Returns the type of pairing offered by this bilinear group.
     */
    @Override
    public BilinearGroup.Type getPairingType() {
        return pairingType;
    }
}
