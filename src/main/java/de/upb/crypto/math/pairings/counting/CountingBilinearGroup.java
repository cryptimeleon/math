package de.upb.crypto.math.pairings.counting;

import de.upb.crypto.math.pairings.generic.BilinearGroup;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.pairings.generic.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.GroupHomomorphism;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.random.interfaces.RandomGenerator;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearGroup;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Objects;

import static de.upb.crypto.math.pairings.generic.BilinearGroup.Type.TYPE_1;
import static de.upb.crypto.math.pairings.generic.BilinearGroup.Type.TYPE_2;

/**
 * A {@link BilinearGroup} implementing a fast, but insecure pairing over {@link Zn}.
 * Allows for counting group operations and (multi-)exponentiations as well as pairings on the bilinear
 * group level.
 * <p>
 * The counting capability is implemented by wrapping two {@link LazyBilinearGroup}s which contain
 * {@link CountingBilinearGroupImpl}s themselves. All operations are executed in both groups,
 * one counts total group operations and one counts each (multi-)exponentiation as one unit.
 * This allows tracking both kinds of data.
 *
 * @author Raphael Heitjohann
 */
public class CountingBilinearGroup implements BilinearGroup {

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
     * The bilinear group responsible for counting total group operations.
     */
    @Represented
    protected LazyBilinearGroup totalBilGroup;

    /**
     * The bilinear group responsible for counting (multi-)exponentiations and group operations outside of those.
     */
    @Represented
    protected LazyBilinearGroup expMultiExpBilGroup;

    protected CountingBilinearMap bilMap;

    /**
     * Initializes this bilinear group with the given security level, pairing type, and group order factoring
     * into the given number of prime factors.
     *
     * @param securityParameter the security level in number of bits
     * @param pairingType the type of pairing that should be offered by this bilinear group
     * @param numPrimeFactors the number of prime factors the group order should have
     */
    public CountingBilinearGroup(int securityParameter, BilinearGroup.Type pairingType, int numPrimeFactors) {
        this.securityParameter = securityParameter;
        this.pairingType = pairingType;

        ArrayList<BigInteger> primeFactors = new ArrayList<>();
        RandomGenerator rnd = RandomGeneratorSupplier.getRnd();
        for (int i = 0; i < numPrimeFactors; i++)
            primeFactors.add(rnd.getRandomPrime(securityParameter));
        
        BigInteger size = primeFactors.stream().reduce(BigInteger.ONE, BigInteger::multiply);
        totalBilGroup = new LazyBilinearGroup(new CountingBilinearGroupImpl(
                securityParameter, pairingType, size, false, false
        ));
        expMultiExpBilGroup = new LazyBilinearGroup(new CountingBilinearGroupImpl(
                securityParameter, pairingType, size, true, true
        ));
        init();
    }

    /**
     *
     * Initializes this bilinear group with the given security level, pairing type and prime group order.
     *
     * @param securityParameter the security level in number of bits
     * @param pairingType the type of pairing that should be offered by this bilinear group
     */
    public CountingBilinearGroup(int securityParameter, BilinearGroup.Type pairingType) {
        this(securityParameter, pairingType, 1);
    }

    public CountingBilinearGroup(Representation repr) {
        ReprUtil.deserialize(this, repr);
        init();
    }

    /**
     * Initializes the underlying bilinear map {@link #bilMap}.
     */
    protected void init() {
        bilMap = new CountingBilinearMap(totalBilGroup.getBilinearMap(), expMultiExpBilGroup.getBilinearMap());
    }

    @Override
    public Group getG1() {
        return new CountingGroup(totalBilGroup.getG1(), expMultiExpBilGroup.getG1());
    }

    @Override
    public Group getG2() {
        return new CountingGroup(totalBilGroup.getG2(), expMultiExpBilGroup.getG2());
    }

    @Override
    public Group getGT() {
        return new CountingGroup(totalBilGroup.getGT(), expMultiExpBilGroup.getGT());
    }

    @Override
    public BilinearMap getBilinearMap() {
        return bilMap;
    }

    @Override
    public GroupHomomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        if (pairingType != TYPE_1 && pairingType != TYPE_2)
            throw new UnsupportedOperationException("Didn't require existence of a group homomorphism");
        return new CountingHomomorphism(
                totalBilGroup.getHomomorphismG2toG1(),
                expMultiExpBilGroup.getHomomorphismG2toG1()
        );
    }

    @Override
    public HashIntoStructure getHashIntoG1() throws UnsupportedOperationException {
        return new CountingHashIntoStructure(totalBilGroup.getHashIntoG1(), expMultiExpBilGroup.getHashIntoG1());
    }

    @Override
    public HashIntoStructure getHashIntoG2() throws UnsupportedOperationException {
        return new CountingHashIntoStructure(totalBilGroup.getHashIntoG2(), expMultiExpBilGroup.getHashIntoG2());

    }

    @Override
    public HashIntoStructure getHashIntoGT() throws UnsupportedOperationException {
        return new CountingHashIntoStructure(totalBilGroup.getHashIntoGT(), expMultiExpBilGroup.getHashIntoGT());

    }

    @Override
    public Integer getSecurityLevel() {
        return securityParameter;
    }

    @Override
    public Type getPairingType() {
        return pairingType;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        CountingBilinearGroup that = (CountingBilinearGroup) other;
        return Objects.equals(totalBilGroup, that.totalBilGroup)
                && Objects.equals(expMultiExpBilGroup, that.expMultiExpBilGroup)
                && Objects.equals(bilMap, that.bilMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalBilGroup, expMultiExpBilGroup, bilMap);
    }

    /**
     * Retrieves number of pairings computed in this bilinear group.
     */
    public long getNumPairings() {
        return bilMap.getNumPairings();
    }

    /**
     * Resets pairing counter.
     */
    public void resetNumPairings() {
        bilMap.resetNumPairings();
    }

    /**
     * Resets all counters, including the ones in groups G1, G2, GT.
     */
    public void resetCounters() {
        resetNumPairings();
        ((CountingGroup) getG1()).resetCounters();
        ((CountingGroup) getG2()).resetCounters();
        ((CountingGroup) getGT()).resetCounters();
    }

    public String formatCounterData() {
        return bilMap.formatCounterData();
    }
}
