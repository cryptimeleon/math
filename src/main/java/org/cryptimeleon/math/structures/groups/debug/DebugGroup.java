package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
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
 * This counting capability is realized by using two {@link LazyGroup}s that each wrap a {@link DebugGroupImpl}.
 * One counts total group operations and squarings, and the other counts (multi-)exponentiations as a single unit
 * (not including group operations and squarings done inside (multi-)exponentiations).
 */
public class DebugGroup implements Group {

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
    public DebugGroup(String name, BigInteger n) {
        groupTotal = new LazyGroup(new DebugGroupImpl(name, n, false, false));
        groupExpMultiExp = new LazyGroup(new DebugGroupImpl(name, n, true, true));
    }

    public DebugGroup(String name, long n) {
        this(name, BigInteger.valueOf(n));
    }

    /**
     * This constructor allows instantiating the {@link DebugGroup} with specific {@link LazyGroup} instances.
     * This can, for example, be used to change the choice of (multi-)exponentiation algorithm by configuring
     * the {@link LazyGroup} instances to use a different (multi-)exponentiation algorithm.
     */
    public DebugGroup(LazyGroup groupTotal, LazyGroup groupExpMultiExp) {
        this.groupTotal = groupTotal;
        this.groupExpMultiExp = groupExpMultiExp;
    }

    public DebugGroup(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public GroupElement getNeutralElement() {
        return new DebugGroupElement(
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
        return new DebugGroupElement(
                this,
                (LazyGroupElement) groupTotal.getUniformlyRandomElement(),
                (LazyGroupElement) groupExpMultiExp.getUniformlyRandomElement()
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
                groupExpMultiExp.wrap(((DebugGroupImpl) groupExpMultiExp.getImpl()).wrap(elem))
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
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumSquarings();
    }

    /**
     * Retrieves number of group inversions including ones done in (multi-)exponentiation algorithms.
     */
    public long getNumInversionsTotal() {
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumInversions();
    }

    /**
     * Retrieves number of group ops including ones done in (multi-)exponentiation algorithms.
     * Does not include squarings.
     */
    public long getNumOpsTotal() {
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumOps();
    }

    /**
     * Retrieves number of group squarings not including ones done in (multi-)exponentiation algorithms.
     */
    public long getNumSquaringsNoExpMultiExp() {
        return ((DebugGroupImpl) groupExpMultiExp.getImpl()).getNumSquarings();
    }

    /**
     * Retrieves number of group inversions not including ones done in (multi-)exponentiation algorithms.
     */
    public long getNumInversionsNoExpMultiExp() {
        return ((DebugGroupImpl) groupExpMultiExp.getImpl()).getNumInversions();
    }

    /**
     * Retrieves number of group ops not including ones done in (multi-)exponentiation algorithms.
     * Does not include squarings.
     */
    public long getNumOpsNoExpMultiExp() {
        return((DebugGroupImpl) groupExpMultiExp.getImpl()).getNumOps();
    }

    /**
     * Retrieves number of group exponentiations done.
     */
    public long getNumExps() {
        return ((DebugGroupImpl) groupExpMultiExp.getImpl()).getNumExps();
    }

    /**
     * Retrieves number of terms of each multi-exponentiation done.
     */
    public List<Integer> getMultiExpTermNumbers() {
        return ((DebugGroupImpl) groupExpMultiExp.getImpl()).getMultiExpTermNumbers();
    }

    /**
     * Retrieves number of retrieved representations of group elements for this group (via getRepresentation()).
     */
    public long getNumRetrievedRepresentations() {
        // one of the groups suffices since we represent both elements
        return ((DebugGroupImpl) groupTotal.getImpl()).getNumRetrievedRepresentations();
    }

    /**
     * Resets all counters.
     */
    public void resetCounters() {
        ((DebugGroupImpl) groupTotal.getImpl()).resetCounters();
        ((DebugGroupImpl) groupExpMultiExp.getImpl()).resetCounters();
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
        DebugGroup other = (DebugGroup) o;
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
