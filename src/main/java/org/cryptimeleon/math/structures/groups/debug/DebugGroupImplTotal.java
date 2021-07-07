package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;

import java.math.BigInteger;
import java.util.HashMap;

/**
 * {@link DebugGroupImpl} implementation that counts operation including those done inside (multi-)exponentiations.
 */
public class DebugGroupImplTotal extends DebugGroupImpl {

    /**
     * Maps the name of each bucket to the actual {@code CountingBucket} object.
     */
    static HashMap<String, CountingBucket> countingBucketMap;

    /**
     * Tracks operation data across all other buckets, including default and all named buckets.
     */
    static CountingBucket overallBucket;

    /**
     * The currently used bucket.
     */
    static CountingBucket currentBucket;

    // Initialization block for static variables
    static {
        countingBucketMap = new HashMap<>();
        countingBucketMap.put("default", new CountingBucket());
        overallBucket = new CountingBucket();
        currentBucket = countingBucketMap.get("default");
    }

    public DebugGroupImplTotal(String name, BigInteger n) {
        super(name, n);
    }

    public DebugGroupImplTotal(Representation repr) {
        super(repr);
    }

    /**
     * Sets the currently used operation count storage bucket to the one with the given name.
     * If a bucket with the given name does not exist, a new one is created.
     * <p>
     * All operations executed after setting a bucket will be counted within that bucket only.
     * <p>
     * The name of the default bucket is "default".
     *
     * @param name the name of the bucket to enable
     */
    public void setBucket(String name) {
        if (!countingBucketMap.containsKey(name)) {
            // if map does not contain bucket with that name, add new one
            countingBucketMap.put(name, new CountingBucket());
        }
        currentBucket = countingBucketMap.get(name);
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
    void incrementNumOps() {
        currentBucket.incrementNumOps();
        overallBucket.incrementNumOps();
    }

    @Override
    void incrementNumInversions() {
        currentBucket.incrementNumInversions();
        overallBucket.incrementNumInversions();
    }

    @Override
    void incrementNumSquarings() {
        currentBucket.incrementNumSquarings();
        overallBucket.incrementNumSquarings();
    }

    @Override
    void incrementNumExps() {
        currentBucket.incrementNumExps();
        overallBucket.incrementNumExps();
    }

    @Override
    void addMultiExpBaseNumber(int numTerms) {
        currentBucket.addMultiExpBaseNumber(numTerms);
        overallBucket.addMultiExpBaseNumber(numTerms);
    }

    @Override
    void incrementNumRetrievedRepresentations() {
        currentBucket.incrementNumRetrievedRepresentations();
        overallBucket.incrementNumRetrievedRepresentations();
    }

    //TODO: Add getter methods with name of bucket
}
