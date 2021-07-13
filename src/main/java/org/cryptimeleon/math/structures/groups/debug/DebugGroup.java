package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.exp.ExpAlgorithm;
import org.cryptimeleon.math.structures.groups.exp.MultiExpAlgorithm;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroup;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Zn-based group that supports counting group operations, inversions, squarings and exponentiations as well as
 * number of terms in each multi-exponentiation.
 * <p>
 * This counting capability is realized by using two {@link LazyGroup}s that each wrap a {@link DebugGroupImpl}.
 * One counts total group operations and squarings, and the other counts (multi-)exponentiations as a single unit
 * (not including group operations and squarings done inside (multi-)exponentiations).
 */
public class DebugGroup implements Group {

    /**
     * Tracks total numbers, meaning that group operations done in (multi-)exp algorithms are also tracked.
     */
    @Represented
    protected LazyGroup groupTotal;

    /**
     * Does not track group operations done in (multi-)exp algorithms, but instead tracks number of exponentiations
     * and multi-exponentiation data.
     */
    @Represented
    protected LazyGroup groupNoExpMultiExp;

    /**
     * Determines which namespace's buckets this group is using.
     */
    @Represented
    protected CountingNamespace countingNamespace;

    /**
     * Initializes the counting group with a given name and size.
     * Group operations only work between groups of the same name and size.
     *
     * @param name the name of the group
     * @param size the desired size of the group
     */
    public DebugGroup(String name, BigInteger size) {
        this(name, size, CountingNamespace.NO_BILGROUP);
    }

    // For DebugBilinearGroup
    DebugGroup(String name, BigInteger size, CountingNamespace cntNamespace) {
        countingNamespace = cntNamespace;
        switch (countingNamespace) {
            case G1:
                groupTotal = new LazyGroup(new DebugGroupImplG1Total(name, size));
                groupNoExpMultiExp = new LazyGroup(new DebugGroupImplG1NoExpMultiExp(name, size));
                break;
            case G2:
                groupTotal = new LazyGroup(new DebugGroupImplG2Total(name, size));
                groupNoExpMultiExp = new LazyGroup(new DebugGroupImplG2NoExpMultiExp(name, size));
                break;
            case GT:
                groupTotal = new LazyGroup(new DebugGroupImplGTTotal(name, size));
                groupNoExpMultiExp = new LazyGroup(new DebugGroupImplGTNoExpMultiExp(name, size));
                break;
            default:
                groupTotal = new LazyGroup(new DebugGroupImplTotal(name, size));
                groupNoExpMultiExp = new LazyGroup(new DebugGroupImplNoExpMultiExp(name, size));
                break;
        }
    }

    public DebugGroup(String name, long size) {
        this(name, BigInteger.valueOf(size));
    }

    /**
     * This constructor allows instantiating the {@link DebugGroup} with specific {@link LazyGroup} instances.
     * This can, for example, be used to change the choice of (multi-)exponentiation algorithm by configuring
     * the {@link LazyGroup} instances to use a different (multi-)exponentiation algorithm.
     */
    public DebugGroup(LazyGroup groupTotal, LazyGroup groupExpMultiExp) {
        this.groupTotal = groupTotal;
        this.groupNoExpMultiExp = groupExpMultiExp;
        this.countingNamespace = CountingNamespace.NO_BILGROUP;
    }

    public DebugGroup(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public GroupElement getNeutralElement() {
        return new DebugGroupElement(
                this,
                (LazyGroupElement) groupTotal.getNeutralElement(),
                (LazyGroupElement) groupNoExpMultiExp.getNeutralElement()
        );
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return groupTotal.size();
    }

    @Override
    public Zn getZn() {
        return groupTotal.getZn();
    }

    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return new DebugGroupElement(
                this,
                (LazyGroupElement) groupTotal.getUniformlyRandomElement(),
                (LazyGroupElement) groupNoExpMultiExp.getUniformlyRandomElement()
        );
    }

    @Override
    public GroupElement restoreElement(Representation repr) {
        return new DebugGroupElement(this, repr);
    }

    public DebugGroupElement wrap(Zn.ZnElement elem) {
        return new DebugGroupElement(
                this,
                groupTotal.wrap(((DebugGroupImpl) groupTotal.getImpl()).wrap(elem)),
                groupNoExpMultiExp.wrap(((DebugGroupImpl) groupNoExpMultiExp.getImpl()).wrap(elem))
        );
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        Optional<Integer> totalLength = groupTotal.getUniqueByteLength();
        Optional<Integer> expMultiExpLength = groupNoExpMultiExp.getUniqueByteLength();
        if (!totalLength.isPresent() || !expMultiExpLength.isPresent()) {
            return Optional.empty();
        } else {
            return Optional.of(totalLength.get() + expMultiExpLength.get());
        }
    }

    @Override
    public boolean isCommutative() {
        return groupTotal.isCommutative();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
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
        ((DebugGroupImpl) groupTotal.getImpl()).setBucket(name);
        ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).setBucket(name);
    }

    /**
     * Retrieves number of group squarings including ones done in (multi-)exponentiation algorithms
     * from the bucket with the given name.
     *
     * @param bucketName the name of the bucket to retrieve number of squarings from
     */
    public long getNumSquaringsTotal(String bucketName) {
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumSquarings(bucketName);
    }

    /**
     * Retrieves number of group inversions including ones done in (multi-)exponentiation algorithms
     * from the bucket with the given name.
     *
     * @param bucketName the name of the bucket to retrieve number of inversions from
     */
    public long getNumInversionsTotal(String bucketName) {
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumInversions(bucketName);
    }

    /**
     * Retrieves number of group ops including ones done in (multi-)exponentiation algorithms
     * from the bucket with the given name.
     * Does not include squarings.
     *
     * @param bucketName the name of the bucket to retrieve number of operations from
     */
    public long getNumOpsTotal(String bucketName) {
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumOps(bucketName);
    }

    /**
     * Retrieves number of group squarings not including ones done in (multi-)exponentiation algorithms
     * from the bucket with the given name.
     *
     * @param bucketName the name of the bucket to retrieve number of squarings from
     */
    public long getNumSquaringsNoExpMultiExp(String bucketName) {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumSquarings(bucketName);
    }

    /**
     * Retrieves number of group inversions not including ones done in (multi-)exponentiation algorithms
     * from the bucket with the given name.
     *
     * @param bucketName the name of the bucket to retrieve number of inversions from
     */
    public long getNumInversionsNoExpMultiExp(String bucketName) {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumInversions(bucketName);
    }

    /**
     * Retrieves number of group ops not including ones done in (multi-)exponentiation algorithms
     * from the bucket with the given name.
     * Does not include squarings.
     *
     * @param bucketName the name of the bucket to retrieve number of operations from
     */
    public long getNumOpsNoExpMultiExp(String bucketName) {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumOps(bucketName);
    }

    /**
     * Retrieves number of group exponentiations done from the bucket with the given name.
     *
     * @param bucketName the name of the bucket to retrieve number of exponentiations from
     */
    public long getNumExps(String bucketName) {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumExps(bucketName);
    }

    /**
     * Retrieves number of terms of each multi-exponentiation done from the bucket with the given name.
     *
     * @param bucketName the name of the bucket to retrieve multi-exponentiation term numbers from
     */
    public List<Integer> getMultiExpTermNumbers(String bucketName) {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getMultiExpTermNumbers(bucketName);
    }

    /**
     * Retrieves number of retrieved representations of group elements for this group (via {@code getRepresentation()})
     * from the bucket with the given name.
     *
     * @param bucketName the name of the bucket to retrieve number of retrieved representations from
     */
    public long getNumRetrievedRepresentations(String bucketName) {
        // one of the groups suffices since we represent both elements
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumRetrievedRepresentations(bucketName);
    }

    /*
    -------------- DEFAULT BUCKET GETTER METHODS BLOCK --------------------------------------------
     */

    /**
     * Retrieves number of group squarings including ones done in (multi-)exponentiation algorithms
     * from the default bucket.
     */
    public long getNumSquaringsTotalDefault() {
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumOpsDefault();
    }

    /**
     * Retrieves number of group inversions including ones done in (multi-)exponentiation algorithms
     * from the default bucket.
     */
    public long getNumInversionsTotalDefault() {
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumInversionsDefault();
    }

    /**
     * Retrieves number of group ops including ones done in (multi-)exponentiation algorithms
     * from the default bucket.
     * Does not include squarings.
     */
    public long getNumOpsTotalDefault() {
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumOpsDefault();
    }

    /**
     * Retrieves number of group squarings not including ones done in (multi-)exponentiation algorithms
     * from the default bucket.
     */
    public long getNumSquaringsNoExpMultiExpDefault() {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumSquaringsDefault();
    }

    /**
     * Retrieves number of group inversions not including ones done in (multi-)exponentiation algorithms
     * from the default bucket.
     */
    public long getNumInversionsNoExpMultiExpDefault() {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumInversionsDefault();
    }

    /**
     * Retrieves number of group ops not including ones done in (multi-)exponentiation algorithms
     * from the default bucket.
     * Does not include squarings.
     */
    public long getNumOpsNoExpMultiExpDefault() {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumOpsDefault();
    }

    /**
     * Retrieves number of group exponentiations done from the default bucket.
     */
    public long getNumExpsDefault() {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumExpsDefault();
    }

    /**
     * Retrieves number of terms of each multi-exponentiation done from the default bucket.
     */
    public List<Integer> getMultiExpTermNumbersDefault() {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getMultiExpTermNumbersDefault();
    }

    /**
     * Retrieves number of retrieved representations of group elements for this group (via {@code getRepresentation()})
     * from the default bucket.
     */
    public long getNumRetrievedRepresentationsDefault() {
        // one of the groups suffices since we represent both elements
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumRetrievedRepresentationsDefault();
    }

    /*
    -------------- ALL BUCKETS GETTER METHODS BLOCK -----------------------------------------------
     */

    /**
     * Retrieves number of group squarings including ones done in (multi-)exponentiation algorithms
     * summed up across all buckets.
     */
    public long getNumSquaringsTotalAllBuckets() {
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumOpsAllBuckets();
    }

    /**
     * Retrieves number of group inversions including ones done in (multi-)exponentiation algorithms
     * summed up across all buckets.
     */
    public long getNumInversionsTotalAllBuckets() {
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumInversionsAllBuckets();
    }

    /**
     * Retrieves number of group ops including ones done in (multi-)exponentiation algorithms
     * summed up across all buckets.
     * Does not include squarings.
     */
    public long getNumOpsTotalAllBuckets() {
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumOpsAllBuckets();
    }

    /**
     * Retrieves number of group squarings not including ones done in (multi-)exponentiation algorithms
     * summed up across all buckets.
     */
    public long getNumSquaringsNoExpMultiExpAllBuckets() {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumSquaringsAllBuckets();
    }

    /**
     * Retrieves number of group inversions not including ones done in (multi-)exponentiation algorithms
     * summed up across all buckets.
     */
    public long getNumInversionsNoExpMultiExpAllBuckets() {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumInversionsAllBuckets();
    }

    /**
     * Retrieves number of group ops not including ones done in (multi-)exponentiation algorithms
     * summed up across all buckets.
     * Does not include squarings.
     */
    public long getNumOpsNoExpMultiExpAllBuckets() {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumOpsAllBuckets();
    }

    /**
     * Retrieves number of group exponentiations done summed up across all buckets.
     */
    public long getNumExpsAllBuckets() {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getNumExpsAllBuckets();
    }

    /**
     * Retrieves number of terms of each multi-exponentiation done across all buckets.
     */
    public List<Integer> getMultiExpTermNumbersAllBuckets() {
        return ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getMultiExpTermNumbersAllBuckets();
    }

    /**
     * Retrieves number of retrieved representations of group elements for this group (via {@code getRepresentation()})
     * summed up across all buckets.
     */
    public long getNumRetrievedRepresentationsAllBuckets() {
        // one of the groups suffices since we represent both elements
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumRetrievedRepresentationsAllBuckets();
    }

    /**
     * Resets all counters for the bucket with the given name.
     */
    public void resetCounters(String bucketName) {
        ((DebugGroupImpl) groupTotal.getImpl()).resetCounters(bucketName);
        ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).resetCounters(bucketName);
    }

    /**
     * Resets all counters for the default bucket.
     */
    public void resetCountersDefault() {
        ((DebugGroupImpl) groupTotal.getImpl()).resetCountersDefault();
        ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).resetCountersDefault();
    }

    /**
     * Resets counters for all buckets.
     */
    public void resetCountersAllBuckets() {
        ((DebugGroupImpl) groupTotal.getImpl()).resetCountersAllBuckets();
        ((DebugGroupImpl) groupNoExpMultiExp.getImpl()).resetCountersAllBuckets();
    }

    /**
     * Formats the count data of the bucket with the given name for printing.
     *
     * @param bucketName the name of the bucket whose data to format for printing
     *
     * @return a string detailing the results of counting
     */
    public String formatCounterData(String bucketName) {
        return formatCounterData(bucketName, true, false);
    }

    /**
     * Formats the counter data of all buckets for printing.
     *
     * @param summaryOnly if true, only formats the summed up results across all buckets; otherwise, outputs results
     *                    of every bucket plus the summary
     *
     * @return a string detailing results of counting
     */
    public String formatCounterData(boolean summaryOnly) {
        StringBuilder result = new StringBuilder();
        if (!summaryOnly) {
            // Union of buckets
            Set<String> bucketNames = ((DebugGroupImpl) groupTotal.getImpl()).getBucketMap().keySet();
            bucketNames.addAll(((DebugGroupImpl) groupNoExpMultiExp.getImpl()).getBucketMap().keySet());
            for (String bucketName : bucketNames) {
                result.append(formatCounterData(bucketName, true, false));
            }
        }
        return result.append(formatCounterDataAllBuckets(true, false)).toString();
    }

    /**
     * Formats the count data of all buckets for printing.
     *
     * @return a string detailing results of counting
     */
    public String formatCounterData() {
        return formatCounterData(false);
    }

    /**
     * Formats the count data of the bucket with the given name for printing.
     *
     * @param bucketName the name of the bucket whose data to format for printing
     * @param printName if true, the name of the bucket is prepended to the data and an additional level of
     *                  indentation is added; otherwise, these things are not done
     * @param addAdditionalTab if true, an additional level of indentation is added.
     *                         Useful for the bilinear map printing.
     *
     * @return a string detailing the results of counting
     */
    String formatCounterData(String bucketName, boolean printName, boolean addAdditionalTab) {
        long totalNumOps = getNumOpsTotal(bucketName);
        long totalNumSqs = getNumSquaringsTotal(bucketName);
        long totalNumInvs = getNumInversionsTotal(bucketName);
        long totalNumOpsSqs = totalNumOps + totalNumSqs;
        long expMultiExpNumOps = totalNumOps - getNumOpsNoExpMultiExp(bucketName);
        long expMultiExpNumSqs = totalNumSqs - getNumSquaringsNoExpMultiExp(bucketName);
        long expMultiExpNumInvs = totalNumInvs - getNumInversionsNoExpMultiExp(bucketName);
        List<Integer> multiExpTerms = getMultiExpTermNumbers(bucketName);

        String tab = "";
        String tab2 = "    ";
        String result = "";
        if (printName) {
            tab = "    ";
            result += String.format("%s\n", bucketName);
        }
        if (addAdditionalTab) {
            tab += "    ";
        }

        return result
                + String.format("%s(Costly) Operations: %d\n", tab, totalNumOpsSqs)
                + String.format("%s%sNon-squarings: %d (%d of which happened during (multi-)exp)\n",
                                tab, tab2, totalNumSqs, expMultiExpNumOps)
                + String.format("%s%sSquarings: %d (%d of which happened during (multi-)exp)\n",
                                tab, tab2, totalNumSqs, expMultiExpNumSqs)
                + String.format("%sInversions: %d (%d of which happened during (multi-)exp)\n",
                                tab, totalNumInvs, expMultiExpNumInvs)
                + String.format("%sExponentiations: %d\n", tab, getNumExps(bucketName))
                + String.format("%sMulti-exponentiations (number of terms in each): %s\n", tab, multiExpTerms)
                + String.format("%sgetRepresentation() calls: %d\n", tab, getNumRetrievedRepresentations(bucketName));
    }

    String formatCounterDataAllBuckets(boolean printName, boolean addAdditionalTab) {
        long totalNumOps = getNumOpsTotalAllBuckets();
        long totalNumSqs = getNumSquaringsTotalAllBuckets();
        long totalNumInvs = getNumInversionsTotalAllBuckets();
        long totalNumOpsSqs = totalNumOps + totalNumSqs;
        long expMultiExpNumOps = totalNumOps - getNumOpsNoExpMultiExpAllBuckets();
        long expMultiExpNumSqs = totalNumSqs - getNumSquaringsNoExpMultiExpAllBuckets();
        long expMultiExpNumInvs = totalNumInvs - getNumInversionsNoExpMultiExpAllBuckets();
        List<Integer> multiExpTerms = getMultiExpTermNumbersAllBuckets();

        String tab = "";
        String tab2 = "    ";
        String result = "";
        if (printName) {
            tab = "    ";
            result += "Combined results of all buckets\n";
        }
        if (addAdditionalTab) {
            tab += "    ";
        }
        return result +
                String.format("%s(Costly) Operations: %d\n", tab, totalNumOpsSqs) +
                String.format("%s%sNon-squarings: %d (%d of which happened during (multi-)exp)\n",
                        tab, tab2, totalNumSqs, expMultiExpNumOps) +
                String.format("%s%sSquarings: %d (%d of which happened during (multi-)exp)\n",
                        tab, tab2, totalNumSqs, expMultiExpNumSqs) +
                String.format("%sInversions: %d (%d of which happened during (multi-)exp)\n",
                        tab, totalNumInvs, expMultiExpNumInvs) +
                String.format("%sExponentiations: %d\n", tab, getNumExpsAllBuckets()) +
                String.format("%sMulti-exponentiations (number of terms in each): %s\n", tab, multiExpTerms) +
                String.format("%sgetRepresentation() calls: %d\n",
                        tab, getNumRetrievedRepresentationsAllBuckets());
    }

    /**
     * Returns the window size used for the non-cached precomputations computed during the exponentiation algorithm.
     */
    public int getExponentiationWindowSize() {
        // assume they both have the same one
        return groupTotal.getExponentiationWindowSize();
    }

    /**
     * Sets the window size used for used for the non-cached precomputations computed during the
     * exponentiation algorithm.
     * <p>
     * A larger window size leads to an exponential increase in the number of precomputations done during
     * exponentiation. As the precomputations affected by this variable are only temporarily stored during execution
     * of the exponentiation algorithm, we do not recommend setting this too high as the cost of computing the
     * whole window quickly exceeds its performance benefits during the actual exponentiation.
     * <p>
     * If you want to change the number of cached precomputations, use {@link this#setPrecomputationWindowSize(int)}.
     */
    public void setExponentiationWindowSize(int exponentiationWindowSize) {
        groupTotal.setExponentiationWindowSize(exponentiationWindowSize);
        groupNoExpMultiExp.setExponentiationWindowSize(exponentiationWindowSize);
    }

    /**
     * Returns the window size used for the precomputations.
     */
    public int getPrecomputationWindowSize() {
        return groupTotal.getPrecomputationWindowSize();
    }

    /**
     * Sets the window size used for the cached precomputations.
     * <p>
     * A larger window size leads to an exponential increase in the number of cached precomputations done but
     * can also improve the performance of later exponentiations.
     */
    public void setPrecomputationWindowSize(int precomputationWindowSize) {
        groupTotal.setPrecomputationWindowSize(precomputationWindowSize);
        groupNoExpMultiExp.setPrecomputationWindowSize(precomputationWindowSize);
    }

    public MultiExpAlgorithm getSelectedMultiExpAlgorithm() {
        return groupTotal.getSelectedMultiExpAlgorithm();
    }

    public void setSelectedMultiExpAlgorithm(MultiExpAlgorithm selectedMultiExpAlgorithm) {
        groupTotal.setSelectedMultiExpAlgorithm(selectedMultiExpAlgorithm);
        groupNoExpMultiExp.setSelectedMultiExpAlgorithm(selectedMultiExpAlgorithm);
    }

    public ExpAlgorithm getSelectedExpAlgorithm() {
        return groupTotal.getSelectedExpAlgorithm();
    }

    public void setSelectedExpAlgorithm(ExpAlgorithm selectedExpAlgorithm) {
        groupTotal.setSelectedExpAlgorithm(selectedExpAlgorithm);
        groupNoExpMultiExp.setSelectedExpAlgorithm(selectedExpAlgorithm);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugGroup other = (DebugGroup) o;
        return Objects.equals(groupTotal, other.groupTotal)
                && Objects.equals(groupNoExpMultiExp, other.groupNoExpMultiExp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupTotal, groupNoExpMultiExp);
    }

    @Override
    public String toString() {
        DebugGroupImpl debugImpl = (DebugGroupImpl) groupTotal.getImpl();
        return this.getClass().getSimpleName() + " with name " + debugImpl.name + " of size " + debugImpl.size();
    }
}
