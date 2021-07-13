package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMapImpl;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Objects;

/**
 * A {@link BilinearMapImpl} implementing a fast, but insecure pairing over {@link Zn}.
 * Allows for counting pairings.
 * <p>
 * The bilinear map works by mapping {@code (Zn,+) x (Zn,+)} to {@code (Zn,+)} via {@code (a,b) -> a*b}
 * (multiplication in Zn).
 * It is insecure since DLOG is trivial in Zn.
 */
public class DebugBilinearMapImpl implements BilinearMapImpl {
    /**
     * The groups underlying the bilinear group.
     */
    protected DebugGroupImpl g1, g2, gt;

    /**
     * Zn of order the bilinear group's size.
     */
    protected Zn zn;

    /**
     * The order of the bilinear group.
     */
    protected BigInteger size;

    /**
     * The type of pairing this bilinear map offers.
     */
    protected BilinearGroup.Type pairingType;

    private static class PairingCounter {
        private long count;

        public PairingCounter() {
            this.count = 0L;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PairingCounter that = (PairingCounter) o;
            return count == that.count;
        }

        @Override
        public int hashCode() {
            return Objects.hash(count);
        }

        public void incNumPairings() {
            ++count;
        }

        public long getNumPairings() {
            return count;
        }
    }

    /**
     * Maps bucket names to the number of pairings stored within that bucket.
     */
    protected static HashMap<String, PairingCounter> numPairingsMap;

    protected static PairingCounter defaultBucket;

    protected static PairingCounter currentBucket;

    protected static PairingCounter allBucketsNumPairings;

    static {
        numPairingsMap = new HashMap<>();
        defaultBucket = new PairingCounter();
        currentBucket = defaultBucket;
        allBucketsNumPairings = new PairingCounter();
    }

    /**
     * Instantiates this bilinear map with the given pairing type, group size, and counting configuration.
     *
     * @param groupSize size of g1, g2, and gt (number of group elements)
     * @param type type of the pairing
     * @param enableExpMultiExpCounting if {@code true}, number of terms in each multi-exponentiation is tracked and
     *                                  group operations within multi-exponentiations are not counted; otherwise
     *                                  the former is not done and group operations within multi-exponentiations
     *                                  are added to the total count.
     *                                  Furthermore, if {@code true}, exponentiations in G1, G2 and GT are counted
     *                                  as a single unit and group operations within exponentiations are not counted;
     *                                  otherwise the former is not done and group operations within exponentiations are
     *                                  added to the total count
     */
    public DebugBilinearMapImpl(BigInteger groupSize, BilinearGroup.Type type, boolean enableExpMultiExpCounting) {
        this.size = groupSize;
        this.zn = new Zn(groupSize);
        this.pairingType = type;
        if (enableExpMultiExpCounting) {
            g1 = new DebugGroupImplG1NoExpMultiExp("G1", groupSize);
            if (type == BilinearGroup.Type.TYPE_1)
                g2 = g1;
            else
                g2 = new DebugGroupImplG2NoExpMultiExp("G2", groupSize);
            gt = new DebugGroupImplGTNoExpMultiExp("GT", groupSize);
        } else {
            g1 = new DebugGroupImplG1Total("G1", groupSize);
            if (type == BilinearGroup.Type.TYPE_1)
                g2 = g1;
            else
                g2 = new DebugGroupImplG2Total("G2", groupSize);
            gt = new DebugGroupImplGTTotal("GT", groupSize);
        }
    }

    @Override
    public GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2, BigInteger exponent) {
        return apply(g1.pow(exponent), g2);
    }

    @Override
    public GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2) {
        if (!(g1 instanceof DebugGroupElementImpl) || !((DebugGroupElementImpl) g1).group.equals(this.g1))
            throw new IllegalArgumentException("first pairing argument is not in " + this.g1.name + ". It's in "
                    + (!(g1 instanceof DebugGroupElementImpl) ? g1.getStructure() : g1 == null ? null : ((DebugGroupElementImpl) g1).group.name));
        if (!(g2 instanceof DebugGroupElementImpl) || !((DebugGroupElementImpl) g2).group.equals(this.g2))
            throw new IllegalArgumentException("first pairing argument is not in " + this.g2.name + ". It's in "
                    + (!(g2 instanceof DebugGroupElementImpl) ? g2.getStructure() : g2 == null ? null : ((DebugGroupElementImpl) g2).group.name));

        GroupElementImpl result = gt.wrap(((DebugGroupElementImpl) g1).elem.mul(((DebugGroupElementImpl) g2).elem));
        incrementNumPairings();
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + String.format("(Zn=%s, pairingType=%s)", zn, pairingType);
    }

    @Override
    public boolean isSymmetric() {
        return pairingType == BilinearGroup.Type.TYPE_1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugBilinearMapImpl that = (DebugBilinearMapImpl) o;
        return pairingType == that.pairingType &&
                Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, pairingType);
    }

    protected void setBucket(String name) {
        currentBucket = putBucketIfAbsent(name);
    }

    protected void setDefaultBucket() {
        currentBucket = defaultBucket;
    }

    protected PairingCounter putBucketIfAbsent(String name) {
        return numPairingsMap.computeIfAbsent(name, kName -> new PairingCounter());
    }

    protected HashMap<String, PairingCounter> getBucketMap() {
        return numPairingsMap;
    }

    /**
     * Retrieves number of pairings computed in this bilinear group for the bucket with the given name.
     */
    protected long getNumPairings(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumPairings();
    }

    protected long getNumPairingsDefault() {
        return defaultBucket.getNumPairings();
    }

    protected long getNumPairingsAllBuckets() {
        return allBucketsNumPairings.getNumPairings();
    }

    /**
     * Resets pairing counter.
     */
    protected void resetNumPairings(String bucketName) {
        numPairingsMap.put(bucketName, new PairingCounter());
    }

    protected void resetNumPairingsDefault() {
        defaultBucket = new PairingCounter();
    }

    protected void resetNumPairingsAllBuckets() {
        allBucketsNumPairings = new PairingCounter();
        numPairingsMap.replaceAll((name, numPairings) -> new PairingCounter());
    }

    /**
     * Increments the pairing counter for the current bucket.
     */
    protected void incrementNumPairings() {
        currentBucket.incNumPairings();
    }
}
