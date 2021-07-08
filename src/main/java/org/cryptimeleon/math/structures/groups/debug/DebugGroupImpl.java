package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.GroupImpl;
import org.cryptimeleon.math.structures.groups.exp.MultiExpTerm;
import org.cryptimeleon.math.structures.groups.exp.Multiexponentiation;
import org.cryptimeleon.math.structures.groups.exp.SmallExponentPrecomputation;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.*;

/**
 * Zn-based group that supports counting group operations, inversions, squarings and exponentiations as well as
 * number of terms in each multi-exponentiation.
 */
abstract class DebugGroupImpl implements GroupImpl {

    /**
     * Name of this group. Group elements between {@code CountingGroupElementImpl} instances only allow for group
     * operations if the groups' names match.
     */
    @Represented
    protected String name;

    /**
     * The Zn underlying this group. Realizes the actual group operations.
     */
    @Represented
    protected Zn zn;

    /**
     * Instantiates this group with the given name and group size and to not count (multi-)exponentiations
     * explicitly (instead only total group operations are counted).
     *
     * @param name a unique name for this group. group operations are only compatible between groups of the same name
     *             and n
     * @param n    the size of this group
     */
    public DebugGroupImpl(String name, BigInteger n) {
        this.name = name;
        this.zn = new Zn(n);
    }

    public DebugGroupImpl(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public GroupElementImpl getNeutralElement() {
        return wrap(zn.getZeroElement());
    }

    @Override
    public GroupElementImpl getUniformlyRandomElement() throws UnsupportedOperationException {
        return wrap(zn.getUniformlyRandomElement());
    }

    @Override
    public GroupElementImpl getUniformlyRandomNonNeutral() throws UnsupportedOperationException {
        return wrap(zn.getUniformlyRandomNonzeroElement());
    }

    @Override
    public GroupElementImpl restoreElement(Representation repr) {
        return wrap(zn.restoreElement(repr));
    }

    @Override
    public GroupElementImpl getGenerator() throws UnsupportedOperationException {
        return wrap(zn.getOneElement());
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return zn.getUniqueByteLength();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, zn);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        DebugGroupImpl that = (DebugGroupImpl) other;
        return Objects.equals(name, that.name)
                && Objects.equals(zn, that.zn);
    }

    @Override
    public String toString() {
        return "DebugGroupImpl with name " + name + " and size " + zn.size();
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return zn.size();
    }

    @Override
    public boolean hasPrimeSize() throws UnsupportedOperationException {
        return zn.hasPrimeSize();
    }

    /**
     * Wraps a {@code ZnElement} in a {@code CountingGroupElementImpl} belonging to this group.
     */
    public DebugGroupElementImpl wrap(Zn.ZnElement elem) {
        return new DebugGroupElementImpl(this, elem);
    }

    @Override
    public double estimateCostInvPerOp() {
        return 1.6;
    }

    protected void incrementNumOps() {
        getCurrentBucket().incrementNumOps();
        getAllBucketsBucket().incrementNumOps();
    }

    protected void incrementNumInversions() {
        getCurrentBucket().incrementNumInversions();
        getAllBucketsBucket().incrementNumInversions();
    }

    protected void incrementNumSquarings() {
        getCurrentBucket().incrementNumSquarings();
        getAllBucketsBucket().incrementNumSquarings();
    }

    protected void incrementNumExps() {
        getCurrentBucket().incrementNumExps();
        getAllBucketsBucket().incrementNumExps();
    }

    protected void addMultiExpBaseNumber(int numTerms) {
        getCurrentBucket().addMultiExpBaseNumber(numTerms);
        getAllBucketsBucket().addMultiExpBaseNumber(numTerms);
    }

    protected void incrementNumRetrievedRepresentations() {
        getCurrentBucket().incrementNumRetrievedRepresentations();
        getAllBucketsBucket().incrementNumRetrievedRepresentations();
    }

    protected long getNumOps(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumOps();
    }

    protected long getNumInversions(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumInversions();
    }

    protected long getNumSquarings(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumSquarings();
    }

    protected long getNumExps(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumExps();
    }

    protected List<Integer> getMultiExpTermNumbers(String bucketName) {
        return putBucketIfAbsent(bucketName).getMultiExpTermNumbers();
    }

    protected long getNumRetrievedRepresentations(String bucketName) {
        return putBucketIfAbsent(bucketName).getNumRetrievedRepresentations();
    }

    protected long getNumOpsAllBuckets() {
        return getAllBucketsBucket().getNumOps();
    }

    protected long getNumInversionsAllBuckets() {
        return getAllBucketsBucket().getNumInversions();
    }

    protected long getNumSquaringsAllBuckets() {
        return getAllBucketsBucket().getNumSquarings();
    }

    protected long getNumExpsAllBuckets() {
        return getAllBucketsBucket().getNumExps();
    }

    protected List<Integer> getMultiExpTermNumbersAllBuckets() {
        return getAllBucketsBucket().getMultiExpTermNumbers();
    }

    protected long getNumRetrievedRepresentationsAllBuckets() {
        return getAllBucketsBucket().getNumRetrievedRepresentations();
    }

    protected void resetCounters(String bucketName) {
        putBucketIfAbsent(bucketName).resetCounters();
    }

    protected void resetCountersAllBuckets() {
        getAllBucketsBucket().resetCounters();
        getBucketMap().forEach((name, bucket) -> bucket.resetCounters());
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
    abstract void setBucket(String name);

    /**
     * Retrieves the bucket with the given name from {@code countingBucketMap},
     * creating a new one if it does not exist yet.
     *
     * @param name the name of the bucket to retrieve
     */
    abstract CountingBucket putBucketIfAbsent(String name);

    abstract CountingBucket getAllBucketsBucket();

    abstract CountingBucket getCurrentBucket();

    abstract Map<String, CountingBucket> getBucketMap();
}
