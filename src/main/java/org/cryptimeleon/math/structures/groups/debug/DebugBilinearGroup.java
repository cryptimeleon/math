package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.HashIntoGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMap;
import org.cryptimeleon.math.structures.groups.exp.ExpAlgorithm;
import org.cryptimeleon.math.structures.groups.exp.MultiExpAlgorithm;
import org.cryptimeleon.math.structures.groups.lazy.LazyBilinearGroup;
import org.cryptimeleon.math.structures.groups.mappings.GroupHomomorphism;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A {@link BilinearGroup} implementing a fast, but insecure pairing over {@link Zn}.
 * Allows for counting group operations and (multi-)exponentiations as well as pairings on the bilinear
 * group level.
 * <p>
 * The counting capability is implemented by wrapping two {@link LazyBilinearGroup}s which contain
 * {@link DebugBilinearGroupImpl}s themselves. All operations are executed in both groups,
 * one counts total group operations and one counts each (multi-)exponentiation as one unit.
 * This allows for tracking both kinds of data.
 *
 */
public class DebugBilinearGroup implements BilinearGroup {

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

    /**
     * The underlying bilinear map used for applying the pairing function and counting it.
     */
    protected DebugBilinearMap bilMap;

    /**
     * The debug group for G1.
     */
    protected DebugGroup g1;
    /**
     * The debug group for G2.
     */
    protected DebugGroup g2;
    /**
     * The debug group for GT.
     */
    protected DebugGroup gT;

    /**
     * Initializes this prime order bilinear group with the given size and pairing type.
     * @param groupSize the size of the group
     * @param pairingType the type of pairing that should be offered by this bilinear group
     */
    public DebugBilinearGroup(BigInteger groupSize, BilinearGroup.Type pairingType) {
        this.securityParameter = groupSize.bitLength();
        this.pairingType = pairingType;
        totalBilGroup = new LazyBilinearGroup(new DebugBilinearGroupImpl(
                groupSize, pairingType, false
        ));
        expMultiExpBilGroup = new LazyBilinearGroup(new DebugBilinearGroupImpl(
                groupSize, pairingType, true
        ));
        init();
    }

    public DebugBilinearGroup(Representation repr) {
        ReprUtil.deserialize(this, repr);
        init();
    }

    /**
     * Initializes the internal debug objects.
     */
    protected void init() {
        bilMap = new DebugBilinearMap(totalBilGroup.getBilinearMap(), expMultiExpBilGroup.getBilinearMap());
        g1 = new DebugGroup(totalBilGroup.getG1(), expMultiExpBilGroup.getG1());
        g2 = new DebugGroup(totalBilGroup.getG2(), expMultiExpBilGroup.getG2());
        gT = new DebugGroup(totalBilGroup.getGT(), expMultiExpBilGroup.getGT());
    }

    @Override
    public Group getG1() {
        return g1;
    }

    @Override
    public Group getG2() {
        return g2;
    }

    @Override
    public Group getGT() {
        return gT;
    }

    @Override
    public BilinearMap getBilinearMap() {
        return bilMap;
    }

    @Override
    public GroupHomomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        if (pairingType != Type.TYPE_1 && pairingType != Type.TYPE_2)
            throw new UnsupportedOperationException("Didn't require existence of a group homomorphism");
        return new DebugHomomorphism(
                totalBilGroup.getHomomorphismG2toG1(),
                expMultiExpBilGroup.getHomomorphismG2toG1()
        );
    }

    @Override
    public HashIntoGroup getHashIntoG1() throws UnsupportedOperationException {
        return new HashIntoDebugGroup(totalBilGroup.getHashIntoG1(), expMultiExpBilGroup.getHashIntoG1());
    }

    @Override
    public HashIntoGroup getHashIntoG2() throws UnsupportedOperationException {
        return new HashIntoDebugGroup(totalBilGroup.getHashIntoG2(), expMultiExpBilGroup.getHashIntoG2());

    }

    @Override
    public HashIntoGroup getHashIntoGT() throws UnsupportedOperationException {
        return new HashIntoDebugGroup(totalBilGroup.getHashIntoGT(), expMultiExpBilGroup.getHashIntoGT());

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
        DebugBilinearGroup that = (DebugBilinearGroup) other;
        return Objects.equals(totalBilGroup, that.totalBilGroup)
                && Objects.equals(expMultiExpBilGroup, that.expMultiExpBilGroup)
                && Objects.equals(bilMap, that.bilMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalBilGroup, expMultiExpBilGroup, bilMap);
    }

    @Override
    public String toString() {
        return "DebugBilinearGroup of type " + pairingType
                + " simulating security level of " + securityParameter + " bits"
                + " using groups of size " + g1.size();
    }

    /**
     * Sets the currently used operation count storage bucket to the one with the given name.
     * If a bucket with the given name does not exist, a new one is created.
     * <p>
     * The bucket is activated across G1, G2, and GT, as well as the pairing counter.
     * <p>
     * All operations executed after setting a bucket will be counted within that bucket only.
     *
     * @param name the name of the bucket to enable
     */
    public void setBucket(String name) {
        g1.setBucket(name);
        g2.setBucket(name);
        gT.setBucket(name);
        bilMap.setBucket(name);
    }

    /**
     * Returns the number of pairings computed in this bilinear group from the bucket with the given name.
     */
    public long getNumPairings(String bucketName) {
        return bilMap.getNumPairings(bucketName);
    }

    /**
     * Returns the number of pairings computed in this bilinear group from the default bucket
     */
    public long getNumPairings() {
        return bilMap.getNumPairings();
    }

    /**
     * Sums up the pairings across all buckets, including the default bucket.
     */
    public long getNumPairingsAllBuckets() {
        return bilMap.getNumPairingsAllBuckets();
    }

    /**
     * Resets pairing counter of the bucket with the given name.
     */
    public void resetNumPairings(String bucketName) {
        bilMap.resetNumPairings(bucketName);
    }

    /**
     * Resets pairing counter of the default bucket.
     */
    public void resetNumPairings() {
        bilMap.resetNumPairings();
    }

    /**
     * Resets pairing counter of all buckets, including the default bucket.
     */
    public void resetNumPairingsAllBuckets() {
        bilMap.resetNumPairingsAllBuckets();
    }

    /**
     * Resets the counters of the bucket with the given name, including the ones in groups G1, G2, GT
     * as well as the pairing counter.
     */
    public void resetCounters(String bucketName) {
        g1.resetCounters(bucketName);
        g2.resetCounters(bucketName);
        gT.resetCounters(bucketName);
        resetNumPairings(bucketName);
    }

    /**
     * Resets the counters of the default bucket, including the ones in groups G1, G2, GT
     * as well as the pairing counter.
     */
    public void resetCounters() {
        g1.resetCounters();
        g2.resetCounters();
        gT.resetCounters();
        resetNumPairings();
    }

    /**
     * Resets counters of all buckets.
     */
    public void resetCountersAllBuckets() {
        g1.resetCountersAllBuckets();
        g2.resetCountersAllBuckets();
        gT.resetCountersAllBuckets();
        resetNumPairingsAllBuckets();
    }

    /**
     * Formats the count data of the bucket with the given name for printing.
     *
     * @param bucketName the name of the bucket whose data to format for printing
     *
     * @return a string detailing the results of counting
     */
    public String formatCounterData(String bucketName) {
        return bilMap.formatCounterData(bucketName);
    }

    /**
     * Formats the count data of the default bucket for printing.
     *
     * @return a string detailing the results of counting
     */
    public String formatCounterData() {
        return bilMap.formatCounterData();
    }

    /**
     * Formats the count data of all buckets for printing.
     *
     * @return a string detailing results of counting
     */
    public String formatCounterDataAllBuckets() {
        return formatCounterDataAllBuckets(false);
    }

    /**
     * Formats the counter data of all buckets for printing.
     *
     * @param summaryOnly if true, only formats the summed up results across all buckets; otherwise, outputs results
     *                    of every bucket plus the summary
     *
     * @return a string detailing results of counting
     */
    public String formatCounterDataAllBuckets(boolean summaryOnly) {
        return bilMap.formatCounterDataAllBuckets(summaryOnly);
    }

    /**
     * Returns the window size used for the non-cached precomputations computed during the exponentiation algorithm
     * if G1, G2, and GT use the same one; otherwise -1.
     */
    public int getExponentiationWindowSize() {
        if (g1.getExponentiationWindowSize() == g2.getExponentiationWindowSize()) {
            if (g2.getExponentiationWindowSize() == gT.getExponentiationWindowSize()) {
                return g1.getExponentiationWindowSize();
            }
        }
        return -1;
    }

    /**
     * Sets the window size used for used for the non-cached precomputations computed during the
     * exponentiation algorithm for G1, G2, and GT at once.
     * <p>
     * A larger window size leads to an exponential increase in the number of precomputations done during
     * exponentiation. As the precomputations affected by this variable are only temporarily stored during execution
     * of the exponentiation algorithm, we do not recommend setting this too high as the cost of computing the
     * whole window quickly exceeds its performance benefits during the actual exponentiation.
     * <p>
     * If you want to change the number of cached precomputations, use {@link this#setPrecomputationWindowSize(int)}.
     */
    public void setExponentiationWindowSize(int exponentiationWindowSize) {
        g1.setExponentiationWindowSize(exponentiationWindowSize);
        g2.setExponentiationWindowSize(exponentiationWindowSize);
        gT.setExponentiationWindowSize(exponentiationWindowSize);
    }

    /**
     * Returns the window size used for the precomputations if G1, G2, and GT use the same one; otherwise -1.
     */
    public int getPrecomputationWindowSize() {
        if (g1.getPrecomputationWindowSize() == g2.getPrecomputationWindowSize()) {
            if (g2.getPrecomputationWindowSize() == gT.getPrecomputationWindowSize()) {
                return g1.getPrecomputationWindowSize();
            }
        }
        return -1;
    }

    /**
     * Sets the window size used for the cached precomputations for G1, G2, and GT at once.
     * <p>
     * A larger window size leads to an exponential increase in the number of cached precomputations done but
     * can also improve the performance of later exponentiations.
     */
    public void setPrecomputationWindowSize(int precomputationWindowSize) {
        g1.setPrecomputationWindowSize(precomputationWindowSize);
        g2.setPrecomputationWindowSize(precomputationWindowSize);
        gT.setPrecomputationWindowSize(precomputationWindowSize);
    }

    /**
     * Returns the selected multi-exponentiation algorithm if G1, G2, and GT use the same one; otherwise null.
     */
    public MultiExpAlgorithm getSelectedMultiExpAlgorithm() {
        if (g1.getSelectedMultiExpAlgorithm() == g2.getSelectedMultiExpAlgorithm()) {
            if (g2.getSelectedMultiExpAlgorithm() == gT.getSelectedMultiExpAlgorithm()) {
                return g1.getSelectedMultiExpAlgorithm();
            }
        }
        return null;
    }

    /**
     * Sets the multi-exponentiation algorithm used for G1, G2, and GT at once.
     */
    public void setSelectedMultiExpAlgorithm(MultiExpAlgorithm selectedMultiExpAlgorithm) {
        g1.setSelectedMultiExpAlgorithm(selectedMultiExpAlgorithm);
        g2.setSelectedMultiExpAlgorithm(selectedMultiExpAlgorithm);
        gT.setSelectedMultiExpAlgorithm(selectedMultiExpAlgorithm);
    }

    /**
     * Returns the selected exponentiation algorithm if G1, G2, and GT use the same one; otherwise null.
     */
    public ExpAlgorithm getSelectedExpAlgorithm() {
        if (g1.getSelectedExpAlgorithm() == g2.getSelectedExpAlgorithm()) {
            if (g2.getSelectedExpAlgorithm() == gT.getSelectedExpAlgorithm()) {
                return g1.getSelectedExpAlgorithm();
            }
        }
        return null;
    }

    /**
     * Sets the exponentiation algorithm used for G1, G2, and GT at once.
     */
    public void setSelectedExpAlgorithm(ExpAlgorithm selectedExpAlgorithm) {
        g1.setSelectedExpAlgorithm(selectedExpAlgorithm);
        g2.setSelectedExpAlgorithm(selectedExpAlgorithm);
        gT.setSelectedExpAlgorithm(selectedExpAlgorithm);
    }
}
