package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link DebugGroupImpl} implementation that counts operation including those done inside (multi-)exponentiations.
 */
public class DebugGroupImplTotal extends DebugGroupImpl {

    /**
     * Maps the name of each bucket to the actual {@code CountingBucket} object.
     */
    protected static HashMap<String, CountingBucket> countingBucketMap;

    /**
     * Tracks operation data across all other buckets, including default and all named buckets.
     */
    protected static CountingBucket allBucketsBucket;

    /**
     * The currently used bucket.
     */
    protected static CountingBucket currentBucket;

    // Initialization block for static variables
    static {
        countingBucketMap = new HashMap<>();
        countingBucketMap.put("default", new CountingBucket());
        allBucketsBucket = new CountingBucket();
        currentBucket = countingBucketMap.get("default");
    }

    public DebugGroupImplTotal(String name, BigInteger n) {
        super(name, n);
    }

    public DebugGroupImplTotal(Representation repr) {
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

    @Override
    protected void setBucket(String name) {
        currentBucket = putBucketIfAbsent(name);
    }

    @Override
    protected CountingBucket putBucketIfAbsent(String name) {
        return countingBucketMap.computeIfAbsent(name, kName -> new CountingBucket());
    }

    @Override
    protected CountingBucket getAllBucketsBucket() {
        return allBucketsBucket;
    }

    @Override
    protected CountingBucket getCurrentBucket() {
        return currentBucket;
    }

    @Override
    protected Map<String, CountingBucket> getBucketMap() {
        return countingBucketMap;
    }
}
