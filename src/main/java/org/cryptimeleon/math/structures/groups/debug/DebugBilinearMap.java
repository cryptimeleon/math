package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMap;
import org.cryptimeleon.math.structures.groups.lazy.LazyBilinearMap;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroup;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link BilinearMap} implementing a fast, but insecure pairing over {@link Zn}.
 * Allows for counting group operations and (multi-)exponentiations as well as pairings on the bilinear
 * group level.
 * <p>
 * The bilinear map works by mapping {@code (Zn,+) x (Zn,+)} to {@code (Zn,+)} via {@code (a,b) -> a*b}
 * (multiplication in Zn).
 * It is insecure since DLOG is trivial in Zn.
 * <p>
 * The counting capability is implemented by wrapping two {@link LazyBilinearMap}s which contain
 * {@link DebugBilinearGroupImpl}s themselves. All operations are executed in both groups,
 * one counts total group operations and one counts each (multi-)exponentiation as one unit.
 * This allows for tracking both kinds of data.
 *
 * @see DebugBilinearGroup
 */
public class DebugBilinearMap implements BilinearMap {

    LazyBilinearMap bilMapTotal;

    LazyBilinearMap bilMapNoExpMultiExp;

    public DebugBilinearMap(LazyBilinearMap bilMapTotal, LazyBilinearMap bilMapNoExpMultiExp) {
        this.bilMapTotal = bilMapTotal;
        this.bilMapNoExpMultiExp = bilMapNoExpMultiExp;
    }

    @Override
    public Group getG1() {
        return new DebugGroup((LazyGroup) bilMapTotal.getG1(), (LazyGroup) bilMapNoExpMultiExp.getG1());
    }

    @Override
    public Group getG2() {
        return new DebugGroup((LazyGroup) bilMapTotal.getG2(), (LazyGroup) bilMapNoExpMultiExp.getG2());
    }

    @Override
    public Group getGT() {
        return new DebugGroup((LazyGroup) bilMapTotal.getGT(), (LazyGroup) bilMapNoExpMultiExp.getGT());
    }

    @Override
    public GroupElement apply(GroupElement g1, GroupElement g2, BigInteger exponent) {
        DebugGroupElement g1Cast = (DebugGroupElement) g1;
        DebugGroupElement g2Cast = (DebugGroupElement) g2;
        LazyGroupElement g1Result = (LazyGroupElement) bilMapTotal.apply(g1Cast.elemTotal, g2Cast.elemTotal, exponent);
        LazyGroupElement g2Result =
                (LazyGroupElement) bilMapNoExpMultiExp.apply(g1Cast.elemExpMultiExp, g2Cast.elemExpMultiExp, exponent);
        return new DebugGroupElement(
                (DebugGroup) getGT(),
                g1Result,
                g2Result
        );
    }

    @Override
    public GroupElement apply(GroupElement g1, GroupElement g2) {
        // We overwrite this to prevent the default method from BilinearMap to be used which would
        // execute an exponentiation with exponent one. This introduces an unnecessary exponentiation into the counting.
        DebugGroupElement g1Cast = (DebugGroupElement) g1;
        DebugGroupElement g2Cast = (DebugGroupElement) g2;
        LazyGroupElement g1Result = (LazyGroupElement) bilMapTotal.apply(g1Cast.elemTotal, g2Cast.elemTotal);
        LazyGroupElement g2Result =
                (LazyGroupElement) bilMapNoExpMultiExp.apply(g1Cast.elemExpMultiExp, g2Cast.elemExpMultiExp);
        return new DebugGroupElement(
                (DebugGroup) getGT(),
                g1Result,
                g2Result
        );
    }

    @Override
    public boolean isSymmetric() {
        return bilMapTotal.isSymmetric() && bilMapNoExpMultiExp.isSymmetric();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        DebugBilinearMap that = (DebugBilinearMap) other;
        return Objects.equals(bilMapTotal, that.bilMapTotal)
                && Objects.equals(bilMapNoExpMultiExp, that.bilMapNoExpMultiExp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bilMapTotal, bilMapNoExpMultiExp);
    }

    /**
     * Sets the currently used operation count storage bucket to the one with the given name.
     * If a bucket with the given name does not exist, a new one is created.
     * <p>
     * All operations executed after setting a bucket will be counted within that bucket only.
     *
     * @param name the name of the bucket to enable
     */
    public void setBucket(String name) {
        ((DebugBilinearMapImpl) bilMapTotal.getImpl()).setBucket(name);
        ((DebugBilinearMapImpl) bilMapNoExpMultiExp.getImpl()).setBucket(name);
    }

    /**
     * Activates the default bucket.
     */
    public void setDefaultBucket() {
        ((DebugBilinearMapImpl) bilMapTotal.getImpl()).setDefaultBucket();
        ((DebugBilinearMapImpl) bilMapNoExpMultiExp.getImpl()).setDefaultBucket();
    }

    /**
     * Retrieves number of pairings computed in this bilinear group from the bucket with the given name.
     * @param bucketName the name of the bucket to obtain pairing numbers from
     */
    public long getNumPairings(String bucketName) {
        // both count, so we need to halve the result
        return ((DebugBilinearMapImpl) bilMapTotal.getImpl()).getNumPairings(bucketName)  / 2;
    }

    /**
     * Retrieves number of parings computed in this bilinear group from the default bucket.
     */
    public long getNumPairings() {
        // both count, so we need to halve the result
        return ((DebugBilinearMapImpl) bilMapTotal.getImpl()).getNumPairings()  / 2;
    }

    /**
     * Sums up pairings across all buckets, including default bucket.
     */
    public long getNumPairingsAllBuckets() {
        // both count, so we need to halve the result
        return ((DebugBilinearMapImpl) bilMapTotal.getImpl()).getNumPairingsAllBuckets() / 2;
    }

    /**
     * Resets pairing counter for the bucket with the given name.
     * @param bucketName the name of the bucket to reset pairing counter for
     */
    public void resetNumPairings(String bucketName) {
        ((DebugBilinearMapImpl) bilMapTotal.getImpl()).resetNumPairings(bucketName);
        ((DebugBilinearMapImpl) bilMapNoExpMultiExp.getImpl()).resetNumPairings(bucketName);
    }

    /**
     * Resets pairing counter for the default bucket.
     */
    public void resetNumPairings() {
        ((DebugBilinearMapImpl) bilMapTotal.getImpl()).resetNumPairings();
        ((DebugBilinearMapImpl) bilMapNoExpMultiExp.getImpl()).resetNumPairings();
    }

    /**
     * Resets pairing counter for all buckets.
     */
    public void resetNumPairingsAllBuckets() {
        ((DebugBilinearMapImpl) bilMapTotal.getImpl()).resetNumPairingsAllBuckets();
        ((DebugBilinearMapImpl) bilMapNoExpMultiExp.getImpl()).resetNumPairingsAllBuckets();
    }

    @Override
    public String toString() {
        return bilMapTotal.toString();
    }

    /**
     * Formats the count data of the bucket with the given name for printing.
     *
     * @param bucketName the name of the bucket whose data to format for printing
     *
     * @return a string detailing the results of counting
     */
    public String formatCounterData(String bucketName) {
        String tab = "    ";
        return String.format("%s\n", bucketName)
                + String.format("%sPairings: %d\n", tab, getNumPairings(bucketName))
                + tab + "G1\n"
                + ((DebugGroup) getG1()).formatCounterData(bucketName, true, false)
                + tab + "G2\n"
                + ((DebugGroup) getG2()).formatCounterData(bucketName, true, false)
                + tab + "GT\n"
                + ((DebugGroup) getGT()).formatCounterData(bucketName, true, false);
    }

    /**
     * Formats the count data of the default bucket for printing.
     *
     * @return a string detailing the results of counting
     */
    public String formatCounterData() {
        String tab = "    ";
        return "Default\n"
                + String.format("%sPairings: %d\n", tab, getNumPairings())
                + tab + "G1\n"
                + ((DebugGroup) getG1()).formatCounterData("", true, true)
                + tab + "G2\n"
                + ((DebugGroup) getG2()).formatCounterData("", true, true)
                + tab + "GT\n"
                + ((DebugGroup) getGT()).formatCounterData("", true, true);
    }

    /**
     * Formats the count data of all buckets for printing.
     *
     * @return a string detailing results of counting
     * 
     * @see this#formatCounterDataAllBuckets(boolean)
     */
    public String formatCounterDataAllBuckets() {
        return formatCounterDataAllBuckets(false);
    }

    /**
     * Formats the counter data of all buckets for printing.
     * <p>
     * The summed up results are added at the end.
     *
     * @param summaryOnly if true, only formats the summed up results across all buckets; otherwise, outputs results
     *                    of every bucket plus the summary
     *
     * @return a string detailing results of counting
     *
     * @see this#formatCounterDataAllBuckets() 
     */
    public String formatCounterDataAllBuckets(boolean summaryOnly)  {
        StringBuilder result = new StringBuilder();
        if (!summaryOnly) {
            result.append(formatCounterData());

            // Find the union of all bucket names
            Set<String> bucketNameSet = new HashSet<>();
            bucketNameSet.addAll(((DebugBilinearMapImpl) bilMapTotal.getImpl()).getBucketMap().keySet());
            bucketNameSet.addAll(((DebugBilinearMapImpl) bilMapNoExpMultiExp.getImpl()).getBucketMap().keySet());
            bucketNameSet.addAll(((DebugBilinearMapImpl) bilMapNoExpMultiExp.getImpl()).g1.getBucketMap().keySet());
            bucketNameSet.addAll(((DebugBilinearMapImpl) bilMapTotal.getImpl()).g1.getBucketMap().keySet());
            bucketNameSet.addAll(((DebugBilinearMapImpl) bilMapNoExpMultiExp.getImpl()).g2.getBucketMap().keySet());
            bucketNameSet.addAll(((DebugBilinearMapImpl) bilMapTotal.getImpl()).g2.getBucketMap().keySet());
            bucketNameSet.addAll(((DebugBilinearMapImpl) bilMapNoExpMultiExp.getImpl()).gt.getBucketMap().keySet());
            bucketNameSet.addAll(((DebugBilinearMapImpl) bilMapTotal.getImpl()).gt.getBucketMap().keySet());

            for (String bucketName : bucketNameSet) {
                result.append(formatCounterData(bucketName));
            }
        }
        return result.append(formatCounterDataAllBucketsOnly()).toString();
    }

    String formatCounterDataAllBucketsOnly() {
        String tab = "    ";
        return "Combined results of all buckets\n"
                + String.format("%sPairings: %d\n", tab, getNumPairingsAllBuckets())
                + tab + "G1\n"
                + ((DebugGroup) getG1()).formatCounterDataAllBuckets(true)
                + tab + "G2\n"
                + ((DebugGroup) getG2()).formatCounterDataAllBuckets(true)
                + tab + "GT\n"
                + ((DebugGroup) getGT()).formatCounterDataAllBuckets(true);
    }
}
