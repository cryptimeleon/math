package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

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
    static CountingBucket allBucketsBucket;

    /**
     * The currently used bucket.
     */
    static CountingBucket currentBucket;

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
    @Override
    public void setBucket(String name) {
        currentBucket = putBucketIfAbsent(name);
    }

    /**
     * Retrieves the bucket with the given name from {@code countingBucketMap},
     * creating a new one if it does not exist yet.
     *
     * @param name the name of the bucket to retrieve
     */
    private CountingBucket putBucketIfAbsent(String name) {
        return countingBucketMap.computeIfAbsent(name, kName -> new CountingBucket());
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
        allBucketsBucket.incrementNumOps();
    }

    @Override
    void incrementNumInversions() {
        currentBucket.incrementNumInversions();
        allBucketsBucket.incrementNumInversions();
    }

    @Override
    void incrementNumSquarings() {
        currentBucket.incrementNumSquarings();
        allBucketsBucket.incrementNumSquarings();
    }

    @Override
    void incrementNumExps() {
        currentBucket.incrementNumExps();
        allBucketsBucket.incrementNumExps();
    }

    @Override
    void addMultiExpBaseNumber(int numTerms) {
        currentBucket.addMultiExpBaseNumber(numTerms);
        allBucketsBucket.addMultiExpBaseNumber(numTerms);
    }

    @Override
    void incrementNumRetrievedRepresentations() {
        currentBucket.incrementNumRetrievedRepresentations();
        allBucketsBucket.incrementNumRetrievedRepresentations();
    }

    @Override
    long getNumOps(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumOps();
    }

    @Override
    long getNumInversions(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumInversions();
    }

    @Override
    long getNumSquarings(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumSquarings();
    }

    @Override
    long getNumExps(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumExps();
    }

    @Override
    List<Integer> getMultiExpTermNumbers(String bucketName) {
        return putBucketIfAbsent(bucketName).getMultiExpTermNumbers();
    }

    @Override
    long getNumRetrievedRepresentations(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumRetrievedRepresentations();
    }

    @Override
    long getNumOpsAllBuckets() {
        return allBucketsBucket.getNumOps();
    }

    @Override
    long getNumInversionsAllBuckets() {
        return allBucketsBucket.getNumInversions();
    }

    @Override
    long getNumSquaringsAllBuckets() {
        return allBucketsBucket.getNumSquarings();
    }

    @Override
    long getNumExpsAllBuckets() {
        return allBucketsBucket.getNumExps();
    }

    @Override
    List<Integer> getMultiExpTermNumbersAllBuckets() {
        return allBucketsBucket.getMultiExpTermNumbers();
    }

    @Override
    long getNumRetrievedRepresentationsAllBuckets() {
        return allBucketsBucket.getNumRetrievedRepresentations();
    }
}
