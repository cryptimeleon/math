package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMapImpl;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
        private final AtomicLong count;

        public PairingCounter() {
            this.count = new AtomicLong(0);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PairingCounter that = (PairingCounter) o;
            return count.equals(that.count);
        }

        @Override
        public int hashCode() {
            return Objects.hash(count);
        }

        public void incNumPairings() {
            count.incrementAndGet();
        }

        public long getNumPairings() {
            return count.get();
        }
    }

    /**
     * Maps bucket names to the number of pairings stored within that bucket.
     */
    private static final ConcurrentHashMap<String, PairingCounter> numPairingsMap;

    private static final PairingCounter defaultBucket;

    private static volatile PairingCounter currentBucket;

    static {
        numPairingsMap = new ConcurrentHashMap<>();
        defaultBucket = new PairingCounter();
        currentBucket = defaultBucket;
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
                // name is G1 to make equals compatible between elements, but need to use G2 class to allow
                //  separate counting
                g2 = new DebugGroupImplG2NoExpMultiExp("G1", groupSize);
            else
                g2 = new DebugGroupImplG2NoExpMultiExp("G2", groupSize);
            gt = new DebugGroupImplGTNoExpMultiExp("GT", groupSize);
        } else {
            g1 = new DebugGroupImplG1Total("G1", groupSize);
            if (type == BilinearGroup.Type.TYPE_1)
                g2 = new DebugGroupImplG2Total("G1", groupSize);
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

    void setBucket(String name) {
        currentBucket = putBucketIfAbsent(name);
    }

    void setDefaultBucket() {
        currentBucket = defaultBucket;
    }

    PairingCounter putBucketIfAbsent(String name) {
        return numPairingsMap.computeIfAbsent(name, kName -> new PairingCounter());
    }

    ConcurrentHashMap<String, PairingCounter> getBucketMap() {
        return numPairingsMap;
    }

    /**
     * Retrieves number of pairings computed in this bilinear group for the bucket with the given name.
     */
    long getNumPairings(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumPairings();
    }

    long getNumPairingsDefault() {
        return defaultBucket.getNumPairings();
    }

    long getNumPairingsAllBuckets() {
        return getBucketMap().reduceValuesToLong(Long.MAX_VALUE, pc -> pc.count.get(), 0L, Long::sum)
                + getNumPairingsDefault();
    }

    /**
     * Resets pairing counter.
     */
    void resetNumPairings(String bucketName) {
        putBucketIfAbsent(bucketName).count.set(0);
    }

    void resetNumPairingsDefault() {
        defaultBucket.count.set(0);
    }

    void resetNumPairingsAllBuckets() {
        resetNumPairingsDefault();
        numPairingsMap.replaceAll((name, numPairings) -> new PairingCounter());
    }

    /**
     * Increments the pairing counter for the current bucket.
     */
    protected void incrementNumPairings() {
        currentBucket.incNumPairings();
    }
}
