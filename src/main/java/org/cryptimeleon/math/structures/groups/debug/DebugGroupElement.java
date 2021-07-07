package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.serialization.ObjectRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.Element;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroup;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroupElement;

import java.math.BigInteger;
import java.util.Objects;

/**
 * An element of {@link DebugGroup}.
 * <p>
 * As {@code DebugGroup} itself consists of two nested groups, {@code DebugGroupElement} also essentially
 * wraps two group elements, one for each nested group. Group operations are done for both.
 * 
 * @see DebugGroup
 */
public class DebugGroupElement implements GroupElement {

    /**
     * The group this element belongs to.
     */
    protected DebugGroup group;

    /**
     * This element as a member of the group responsible for counting total group operations.
     */
    protected LazyGroupElement elemTotal;

    /**
     * This element as a member of the group responsible for counting (multi-)exponentiations.
     */
    protected LazyGroupElement elemExpMultiExp;

    /**
     * Initializes this group element as belonging to the given group and wrapping the two given group elements.
     *
     * @param group the group this element should belong to
     * @param elemTotal the version of this group element belonging to the group counting total group operations
     * @param elemExpMultiExp the version of this group element belonging to the group counting (multi)-exponentiations
     */
    public DebugGroupElement(DebugGroup group, LazyGroupElement elemTotal, LazyGroupElement elemExpMultiExp) {
        this.group = group;
        this.elemTotal = elemTotal;
        this.elemExpMultiExp = elemExpMultiExp;
    }

    public DebugGroupElement(DebugGroup group, Representation repr) {
        ObjectRepresentation objRepr = repr.obj();
        this.group = group;
        elemTotal = (LazyGroupElement) group.groupTotal.restoreElement(objRepr.get("elemTotal"));
        elemExpMultiExp = (LazyGroupElement) group.groupNoExpMultiExp.restoreElement(objRepr.get("elemExpMultiExp"));
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation repr = new ObjectRepresentation();
        repr.put("elemTotal", elemTotal.getRepresentation());
        repr.put("elemExpMultiExp", elemExpMultiExp.getRepresentation());
        return repr;
    }

    @Override
    public Group getStructure() {
        return new DebugGroup(
                (LazyGroup) elemTotal.getStructure(),
                (LazyGroup) elemExpMultiExp.getStructure()
        );
    }

    @Override
    public GroupElement inv() {
        return new DebugGroupElement(
                group,
                (LazyGroupElement) elemTotal.inv(),
                (LazyGroupElement) elemExpMultiExp.inv()
        );
    }

    @Override
    public GroupElement op(Element e) throws IllegalArgumentException {
        if (e == null)
            throw new IllegalArgumentException("Argument element is null");
        if (e.getClass() != this.getClass())
            throw new IllegalArgumentException("Argument element is not a CountingGroupElement");
        DebugGroupElement other = (DebugGroupElement) e;
        return new DebugGroupElement(
                group,
                (LazyGroupElement) elemTotal.op(other.elemTotal),
                (LazyGroupElement) elemExpMultiExp.op(other.elemExpMultiExp)
        );
    }

    @Override
    public GroupElement pow(BigInteger exponent) {
        return new DebugGroupElement(
                group,
                (LazyGroupElement) elemTotal.pow(exponent),
                (LazyGroupElement) elemExpMultiExp.pow(exponent)
        );
    }

    @Override
    public GroupElement precomputePow() {
        return new DebugGroupElement(
                group,
                (LazyGroupElement) elemTotal.precomputePow(),
                (LazyGroupElement) elemExpMultiExp.precomputePow()
        );
    }

    @Override
    public GroupElement precomputePow(int windowSize) {
        return new DebugGroupElement(
                group,
                (LazyGroupElement) elemTotal.precomputePow(windowSize),
                (LazyGroupElement) elemExpMultiExp.precomputePow(windowSize)
        );
    }

    /**
     * Since asynchronous computation makes count data unreliable, this method works like {@link #computeSync()}.
     *
     * @see #computeSync()
     */
    @Override
    public GroupElement compute() {
        // counting requires synchronization so we always do computeSync
        return new DebugGroupElement(
                group,
                (LazyGroupElement) elemTotal.computeSync(),
                (LazyGroupElement) elemExpMultiExp.computeSync()
        );
    }

    @Override
    public GroupElement computeSync() {
        return new DebugGroupElement(
                group,
                (LazyGroupElement) elemTotal.computeSync(),
                (LazyGroupElement) elemExpMultiExp.computeSync()
        );
    }

    @Override
    public boolean isComputed() {
        return elemTotal.isComputed() && elemExpMultiExp.isComputed();
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return elemExpMultiExp.updateAccumulator(elemTotal.updateAccumulator(accumulator));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugGroupElement other = (DebugGroupElement) o;
        return Objects.equals(group, other.group)
                && Objects.equals(elemTotal, other.elemTotal)
                && Objects.equals(elemExpMultiExp, other.elemExpMultiExp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, elemTotal, elemExpMultiExp);
    }

    @Override
    public String toString() {
        return group + " element " + elemTotal.computeSync().getRepresentation() + ";"
                + elemExpMultiExp.computeSync().getRepresentation();
    }
}
