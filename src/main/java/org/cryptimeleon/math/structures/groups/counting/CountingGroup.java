package org.cryptimeleon.math.structures.groups.counting;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.lazy.ConstLazyGroupElement;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroup;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Zn-based group that supports counting group operations, inversions, squarings and exponentiations as well as
 * number of terms in each multi-exponentiation.
 * <p>
 * This counting capability is realized by using two {@link LazyGroup}s that each wrap a {@link CountingGroupImpl}.
 * One counts total group operations and squarings, and the other counts (multi-)exponentiations as a single unit
 * (not including group operations and squarings done inside (multi-)exponentiations).
 */
public class CountingGroup implements Group {

    /**
     * Tracks total numbers, meaning that group operations done in (multi-)exp algorithms are also tracked.
     */
    @Represented
    LazyGroup groupTotal;

    /**
     * Does not track group operations done in (multi-)exp algorithms, but instead tracks number of exponentiations
     * and multi-exponentiation data.
     */
    @Represented
    LazyGroup groupExpMultiExp;

    /**
     * Initializes the counting group with a given name and size.
     * Group operations only work between groups of the same name and size.
     *
     * @param name the name of the group
     * @param n the desired size of the group
     */
    public CountingGroup(String name, BigInteger n) {
        groupTotal = new LazyGroup(new CountingGroupImpl(name, n, false, false));
        groupExpMultiExp = new LazyGroup(new CountingGroupImpl(name, n, true, true));
    }

    public CountingGroup(String name, long n) {
        this(name, BigInteger.valueOf(n));
    }

    /**
     * This constructor allows instantiating the {@link CountingGroup} with specific {@link LazyGroup} instances.
     * This can, for example, be used to change the choice of (multi-)exponentiation algorithm by configuring
     * the {@link LazyGroup} instances to use a different (multi-)exponentiation algorithm.
     */
    public CountingGroup(LazyGroup groupTotal, LazyGroup groupExpMultiExp) {
        this.groupTotal = groupTotal;
        this.groupExpMultiExp = groupExpMultiExp;
    }

    public CountingGroup(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public GroupElement getNeutralElement() {
        return new CountingGroupElement(
                this,
                (LazyGroupElement) groupTotal.getNeutralElement(),
                (LazyGroupElement) groupExpMultiExp.getNeutralElement()
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
        return new CountingGroupElement(
                this,
                (LazyGroupElement) groupTotal.getUniformlyRandomElement(),
                (LazyGroupElement) groupExpMultiExp.getUniformlyRandomElement()
        );
    }

    @Override
    public GroupElement restoreElement(Representation repr) {
        return new CountingGroupElement(this, repr);
    }

    public CountingGroupElement wrap(Zn.ZnElement elem) {
        return new CountingGroupElement(
                this,
                new ConstLazyGroupElement(groupTotal, ((CountingGroupImpl) groupTotal.getImpl()).wrap(elem)),
                new ConstLazyGroupElement(groupExpMultiExp, ((CountingGroupImpl) groupExpMultiExp.getImpl()).wrap(elem))
        );
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        Optional<Integer> totalLength = groupTotal.getUniqueByteLength();
        Optional<Integer> expMultiExpLength = groupExpMultiExp.getUniqueByteLength();
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
     * Retrieves number of group squarings including ones done in (multi-)exponentiation algorithms.
     */
    public long getNumSquaringsTotal() {
        return ((CountingGroupImpl) groupTotal.getImpl()).getNumSquarings();
    }

    /**
     * Retrieves number of group inversions including ones done in (multi-)exponentiation algorithms.
     */
    public long getNumInversionsTotal() {
        return ((CountingGroupImpl) groupTotal.getImpl()).getNumInversions();
    }

    /**
     * Retrieves number of group ops including ones done in (multi-)exponentiation algorithms.
     * Does not include squarings.
     */
    public long getNumOpsTotal() {
        return ((CountingGroupImpl) groupTotal.getImpl()).getNumOps();
    }

    /**
     * Retrieves number of group squarings not including ones done in (multi-)exponentiation algorithms.
     */
    public long getNumSquaringsNoExpMultiExp() {
        return ((CountingGroupImpl) groupExpMultiExp.getImpl()).getNumSquarings();
    }

    /**
     * Retrieves number of group inversions not including ones done in (multi-)exponentiation algorithms.
     */
    public long getNumInversionsNoExpMultiExp() {
        return ((CountingGroupImpl) groupExpMultiExp.getImpl()).getNumInversions();
    }

    /**
     * Retrieves number of group ops not including ones done in (multi-)exponentiation algorithms.
     * Does not include squarings.
     */
    public long getNumOpsNoExpMultiExp() {
        return((CountingGroupImpl) groupExpMultiExp.getImpl()).getNumOps();
    }

    /**
     * Retrieves number of group exponentiations done.
     */
    public long getNumExps() {
        return ((CountingGroupImpl) groupExpMultiExp.getImpl()).getNumExps();
    }

    /**
     * Retrieves number of terms of each multi-exponentiation done.
     */
    public List<Integer> getMultiExpTermNumbers() {
        return ((CountingGroupImpl) groupExpMultiExp.getImpl()).getMultiExpTermNumbers();
    }

    /**
     * Retrieves number of retrieved representations of group elements for this group (via getRepresentation()).
     */
    public long getNumRetrievedRepresentations() {
        // one of the groups suffices since we represent both elements
        return ((CountingGroupImpl) groupTotal.getImpl()).getNumRetrievedRepresentations();
    }

    /**
     * Resets all counters.
     */
    public void resetCounters() {
        ((CountingGroupImpl) groupTotal.getImpl()).resetCounters();
        ((CountingGroupImpl) groupExpMultiExp.getImpl()).resetCounters();
    }

    /**
     * Formats the count data for printing.
     * @return a string detailing the results of counting
     */
    public String formatCounterData() {
        return "------- Operation data for " + toString() + " -------\n"
                + "----- Total group operation data: -----\n"
                + "    Number of Group Operations: " + getNumOpsTotal() + "\n"
                + "    Number of Group Inversions: " + getNumInversionsTotal() + "\n"
                + "    Number of Group Squarings: " + getNumSquaringsTotal() + "\n"
                + "----- Group operation data without operations done in (multi-)exp algorithms: -----\n"
                + "    Number of Group Operations: " + getNumOpsNoExpMultiExp() + "\n"
                + "    Number of Group Inversions: " + getNumInversionsNoExpMultiExp() + "\n"
                + "    Number of Group Squarings: " + getNumSquaringsNoExpMultiExp() + "\n"
                + "----- Other data: -----\n"
                + "    Number of exponentiations: " + getNumExps() + "\n"
                + "    Number of terms in each multi-exponentiation: " + getMultiExpTermNumbers() + "\n"
                + "    Number of retrieved representations (via getRepresentation()): "
                + getNumRetrievedRepresentations() + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountingGroup other = (CountingGroup) o;
        return Objects.equals(groupTotal, other.groupTotal)
                && Objects.equals(groupExpMultiExp, other.groupExpMultiExp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupTotal, groupExpMultiExp);
    }

    @Override
    public String toString() {
        return "CountingGroup(" + groupTotal + ";" + groupExpMultiExp + ")";
    }
}
