package de.upb.crypto.math.pairings.counting;

import de.upb.crypto.math.interfaces.mappings.impl.GroupHomomorphismImpl;
import de.upb.crypto.math.interfaces.mappings.impl.HashIntoGroupImpl;
import de.upb.crypto.math.pairings.generic.BilinearGroup;
import de.upb.crypto.math.pairings.generic.BilinearGroupImpl;
import de.upb.crypto.math.pairings.generic.BilinearMapImpl;
import de.upb.crypto.math.random.interfaces.RandomGenerator;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Objects;

import static de.upb.crypto.math.pairings.generic.BilinearGroup.Type.TYPE_1;
import static de.upb.crypto.math.pairings.generic.BilinearGroup.Type.TYPE_2;

/**
 * A {@link BilinearGroupImpl} implementing a fast, but insecure pairing over {@link Zn}.
 * Allows for selecting between counting total group operations, or (multi-)exponentiations plus group operations
 * outside of (multi-)exponentiations.
 * <p>
 * Usually, you should be using {@link CountingBilinearGroup} instead of this class as it provides pairing counting
 * capabilities and allows for tracking both kinds of group operation data at once.
 * 
 * @see CountingBilinearGroup
 */
public class CountingBilinearGroupImpl implements BilinearGroupImpl {

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
     * Whether to count exponentiations as a single unit. If set to true, group operations in those exponentiations
     * will not be counted.
     */
    @Represented
    protected Boolean enableExpCounting;

    /**
     * Whether to count multi-exponentiations as a single unit. If set to true, group operations in those
     * multi-exponentiations will not be counted.
     */
    @Represented
    protected Boolean enableMultiExpCounting;

    /**
     * The underlying bilinear map used for applying the pairing function and counting it.
     */
    CountingBilinearMapImpl bilinearMapImpl;

    public CountingBilinearGroupImpl(int securityParameter, BilinearGroup.Type pairingType, int numPrimeFactors,
                                     boolean enableExpCounting, boolean enableMultiExpCounting) {
        this.securityParameter = securityParameter;
        this.pairingType = pairingType;
        this.enableExpCounting = enableExpCounting;
        this.enableMultiExpCounting = enableMultiExpCounting;

        ArrayList<BigInteger> primeFactors = new ArrayList<>();
        RandomGenerator rnd = RandomGeneratorSupplier.getRnd();
        for (int i = 0; i < numPrimeFactors; i++)
            primeFactors.add(rnd.getRandomPrime(securityParameter));

        this.size = primeFactors.stream().reduce(BigInteger.ONE, BigInteger::multiply);
        init();
    }

    public CountingBilinearGroupImpl(int securityParameter, BilinearGroup.Type pairingType, int numPrimeFactors) {
        this(securityParameter, pairingType, numPrimeFactors, false, false);
    }

    public CountingBilinearGroupImpl(int securityParameter, BilinearGroup.Type pairingType) {
        this(securityParameter, pairingType, 1);
    }

    public CountingBilinearGroupImpl(int securityParameter, BilinearGroup.Type pairingType, BigInteger size,
                                     boolean enableExpCounting, boolean enableMultiExpCounting) {
        this.securityParameter = securityParameter;
        this.pairingType = pairingType;
        this.enableExpCounting = enableExpCounting;
        this.enableMultiExpCounting = enableMultiExpCounting;
        this.size = size;
        init();
    }

    protected void init() {
        bilinearMapImpl = new CountingBilinearMapImpl(pairingType, size, enableExpCounting, enableMultiExpCounting);
    }

    public CountingBilinearGroupImpl(Representation repr) {
        ReprUtil.deserialize(this, repr);
        init();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        CountingBilinearGroupImpl that = (CountingBilinearGroupImpl) other;
        return Objects.equals(pairingType, that.pairingType)
                && Objects.equals(size, that.size)
                && Objects.equals(enableExpCounting, that.enableExpCounting)
                && Objects.equals(enableMultiExpCounting, that.enableMultiExpCounting);
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
    public CountingGroupImpl getG1() {
        return bilinearMapImpl.g1;
    }

    @Override
    public CountingGroupImpl getG2() {
        return bilinearMapImpl.g2;
    }

    @Override
    public CountingGroupImpl getGT() {
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
        return new CountingIsomorphismImpl(getG2(), getG1());
    }

    @Override
    public HashIntoGroupImpl getHashIntoG1() throws UnsupportedOperationException {
        return new HashIntoCountingGroupImpl(getG1());
    }

    @Override
    public HashIntoGroupImpl getHashIntoG2() throws UnsupportedOperationException {
        return new HashIntoCountingGroupImpl(getG2());
    }

    @Override
    public HashIntoGroupImpl getHashIntoGT() throws UnsupportedOperationException {
        return new HashIntoCountingGroupImpl(getGT());
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
