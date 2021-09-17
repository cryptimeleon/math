package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.exp.MultiExpTerm;
import org.cryptimeleon.math.structures.groups.exp.Multiexponentiation;
import org.cryptimeleon.math.structures.groups.exp.SmallExponentPrecomputation;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link DebugGroupImpl} implementation that counts operations not done inside (multi-)exponentiations
 * and counts (multi-)exponentiations as their own unit.
 */
public class DebugGroupImplNoExpMultiExp extends DebugGroupImpl {

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
    private static volatile CountingBucket currentBucket;

    // Initialization block for variables
    static {
        countingBucketMap = new ConcurrentHashMap<>();
        defaultBucket = new CountingBucket();
        currentBucket = defaultBucket;
    }

    public DebugGroupImplNoExpMultiExp(String name, BigInteger n) {
        super(name, n);
    }

    public DebugGroupImplNoExpMultiExp(Representation repr) {
        super(repr);
    }

    @Override
    public GroupElementImpl exp(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation) {
        // this method counts the exponentiation only
        return base.pow(exponent);
    }

    @Override
    public GroupElementImpl multiexp(Multiexponentiation mexp) {
        // This method is only used if enableMultiExpCounting is set to true; hence, we count
        // the multi-exponentiation done.
        DebugGroupElementImpl result = (DebugGroupElementImpl) mexp.getConstantFactor().orElse(getNeutralElement());
        for (MultiExpTerm term : mexp.getTerms()) {
            // Use methods where we can disable counting since we only want to count the multi-exponentiation here
            result = (DebugGroupElementImpl) result
                    .op(((DebugGroupElementImpl) term.getBase()).pow(term.getExponent(), false), false);
        }
        addMultiExpBaseNumber(mexp.getTerms().size());
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
