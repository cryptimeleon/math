package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.exp.MultiExpTerm;
import org.cryptimeleon.math.structures.groups.exp.Multiexponentiation;
import org.cryptimeleon.math.structures.groups.exp.SmallExponentPrecomputation;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

/**
 * {@link DebugGroupImpl} implementation that counts operations not done inside (multi-)exponentiations
 * and counts (multi-)exponentiations as their own unit.
 */
public class DebugGroupImplNoExpMultiExp extends DebugGroupImpl {

    /**
     * Maps the name of each bucket to the actual {@code CountingBucket} object.
     */
    static HashMap<String, CountingBucket> countingBucketMap;

    /**
     * The default counting bucket used whenever no named bucket is selected.
     */
    static CountingBucket defaultBucket;

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

    public DebugGroupImplNoExpMultiExp(String name, BigInteger n) {
        super(name, n);
    }

    public DebugGroupImplNoExpMultiExp(Representation repr) {
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
     * Retrieves the bucket from {@code countingBucketMap}, creating a new one if it does not exist yet.
     * @param name the name of the bucket to retrieve
     */
    private CountingBucket putBucketIfAbsent(String name) {
        return countingBucketMap.computeIfAbsent(name, kName -> new CountingBucket());
    }

    @Override
    public GroupElementImpl exp(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation) {
        return base.pow(exponent); // this method already counts the exponentiation
    }

    @Override
    public GroupElementImpl multiexp(Multiexponentiation mexp) {
        // This method is only used if enableMultiExpCounting is set to true; hence, we count
        // the multi-exponentiation done.
        DebugGroupElementImpl result = (DebugGroupElementImpl) mexp.getConstantFactor().orElse(getNeutralElement());
        for (MultiExpTerm term : mexp.getTerms()) {
            // Use methods where we can disable counting since we only want to count the multi-exponentiation here
            result = (DebugGroupElementImpl) result
                    .op(((DebugGroupElementImpl) term.getBase()).pow(term.getExponent(),false), false);
        }
        currentBucket.addMultiExpBaseNumber(mexp.getTerms().size());
        return result;
    }

    @Override
    public boolean implementsOwnExp() {
        return true;
    }

    @Override
    public boolean implementsOwnMultiExp() {
        return true;
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
