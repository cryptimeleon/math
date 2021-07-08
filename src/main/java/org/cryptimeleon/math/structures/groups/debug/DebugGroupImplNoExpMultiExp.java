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

/**
 * {@link DebugGroupImpl} implementation that counts operations not done inside (multi-)exponentiations
 * and counts (multi-)exponentiations as their own unit.
 */
public class DebugGroupImplNoExpMultiExp extends DebugGroupImpl {

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
