package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link DebugGroupImpl} implementation that counts operations not done inside (multi-)exponentiations
 * and counts (multi-)exponentiations as their own unit.
 * Used exclusively to represent GT in {@link DebugBilinearGroup} to allow for separate counting from G1 and G2.
 */
public class DebugGroupImplGTNoExpMultiExp extends DebugGroupImplNoExpMultiExp {
    /**
     * Maps the name of each bucket to the actual {@code CountingBucket} object.
     */
    protected static HashMap<String, CountingBucket> countingBucketMap;

    /**
     * The default bucket.
     */
    protected static CountingBucket defaultBucket;

    /**
     * The currently used bucket.
     */
    protected static CountingBucket currentBucket;

    // Initialization block for variables
    static {
        countingBucketMap = new HashMap<>();
        defaultBucket = new CountingBucket();
        currentBucket = defaultBucket;
    }

    public DebugGroupImplGTNoExpMultiExp(String name, BigInteger n) {
        super(name, n);
    }

    public DebugGroupImplGTNoExpMultiExp(Representation repr) {
        super(repr);
    }

    /**
     * Sets the currently used operation count storage bucket to the one with the given name.
     * If a bucket with the given name does not exist, a new one is created.
     * <p>
     * All operations executed after setting a bucket will be counted within that bucket only.
     *
     * @param name the name of the bucket to enable
     */
    void setBucket(String name) {
        currentBucket = putBucketIfAbsent(name);
    }

    /**
     * Sets the currently used operation count storage bucket to the default one.
     */
    void setDefaultBucket() {
        currentBucket = getDefaultBucket();
    }

    /**
     * Retrieves the bucket with the given name from {@code countingBucketMap},
     * creating a new one if it does not exist yet.
     *
     * @param name the name of the bucket to retrieve
     */
    CountingBucket putBucketIfAbsent(String name) {
        return getBucketMap().computeIfAbsent(name, kName -> new CountingBucket());
    }

    CountingBucket getCurrentBucket() {
        return currentBucket;
    }

    CountingBucket getDefaultBucket() {
        return defaultBucket;
    }

    Map<String, CountingBucket> getBucketMap() {
        return countingBucketMap;
    }
}
