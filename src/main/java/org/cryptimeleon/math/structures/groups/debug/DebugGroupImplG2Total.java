package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link DebugGroupImpl} implementation that counts operations including those done inside (multi-)exponentiations.
 * Used exclusively to represent G2 in {@link DebugBilinearGroup} to allow for separate counting from G1 and GT.
 */
public class DebugGroupImplG2Total extends DebugGroupImplTotal {

    /**
     * Maps the name of each bucket to the actual {@code CountingBucket} object.
     */
    private static final ConcurrentHashMap<String, CountingBucket> countingBucketMap;

    /**
     * The default bucket.
     */
    private static final CountingBucket defaultBucket;

    /**
     * The currently used bucket.
     */
    private static CountingBucket currentBucket;

    // Initialization block for variables
    static {
        countingBucketMap = new ConcurrentHashMap<>();
        defaultBucket = new CountingBucket();
        currentBucket = defaultBucket;
    }

    public DebugGroupImplG2Total(String name, BigInteger n) {
        super(name, n);
    }

    public DebugGroupImplG2Total(Representation repr) {
        super(repr);
    }

    @Override
    public boolean implementsOwnExp() {
        return false;
    }

    @Override
    public boolean implementsOwnMultiExp() {
        return false;
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
        currentBucket = defaultBucket;
    }

    /**
     * Retrieves the bucket with the given name from {@code countingBucketMap},
     * creating a new one if it does not exist yet.
     *
     * @param name the name of the bucket to retrieve
     */
    CountingBucket putBucketIfAbsent(String name) {
        return countingBucketMap.computeIfAbsent(name, kName -> new CountingBucket());
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