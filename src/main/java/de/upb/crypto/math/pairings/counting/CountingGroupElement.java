package de.upb.crypto.math.pairings.counting;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyGroupElement;

import java.math.BigInteger;
import java.util.Objects;

/**
 * An element of {@link CountingGroup}.
 * <p>
 * As {@code CountingGroup} itself consists of two nested groups, {@code CountingGroupElement} also essentially
 * wraps two group elements, one for each nested group. Group operations are done for both.
 * 
 * @see CountingGroup
 *
 * @author Raphael Heitjohann
 */
public class CountingGroupElement implements GroupElement {

    /**
     * The group this element belongs to.
     */
    protected CountingGroup group;

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
    public CountingGroupElement(CountingGroup group, LazyGroupElement elemTotal, LazyGroupElement elemExpMultiExp) {
        this.group = group;
        this.elemTotal = elemTotal;
        this.elemExpMultiExp = elemExpMultiExp;
    }

    public CountingGroupElement(Representation repr) {
        ObjectRepresentation objRepr = repr.obj();
        group = new CountingGroup(objRepr.get("group"));
        elemTotal = (LazyGroupElement) group.groupTotal.getElement(objRepr.get("elemTotal"));
        elemExpMultiExp = (LazyGroupElement) group.groupExpMultiExp.getElement(objRepr.get("elemExpMultiExp"));
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation repr = new ObjectRepresentation();
        repr.put("group", group.getRepresentation());
        repr.put("elemTotal", elemTotal.getRepresentation());
        repr.put("elemExpMultiExp", elemExpMultiExp.getRepresentation());
        return repr;
    }

    @Override
    public Group getStructure() {
        return new CountingGroup(
                (LazyGroup) elemTotal.getStructure(),
                (LazyGroup) elemExpMultiExp.getStructure()
        );
    }

    @Override
    public GroupElement inv() {
        return new CountingGroupElement(
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
        CountingGroupElement other = (CountingGroupElement) e;
        return new CountingGroupElement(
                group,
                (LazyGroupElement) elemTotal.op(other.elemTotal),
                (LazyGroupElement) elemExpMultiExp.op(other.elemExpMultiExp)
        );
    }

    @Override
    public GroupElement pow(BigInteger exponent) {
        return new CountingGroupElement(
                group,
                (LazyGroupElement) elemTotal.pow(exponent),
                (LazyGroupElement) elemExpMultiExp.pow(exponent)
        );
    }

    @Override
    public GroupElement precomputePow() {
        return new CountingGroupElement(
                group,
                (LazyGroupElement) elemTotal.precomputePow(),
                (LazyGroupElement) elemExpMultiExp.precomputePow()
        );
    }

    @Override
    public GroupElement precomputePow(int windowSize) {
        return new CountingGroupElement(
                group,
                (LazyGroupElement) elemTotal.precomputePow(windowSize),
                (LazyGroupElement) elemExpMultiExp.precomputePow(windowSize)
        );
    }

    /**
     * Since asynchronous computation makes count data unreliable, this method works like {@link #computeSync()}.
     * <p>
     * @inheritDoc
     */
    @Override
    public GroupElement compute() {
        // counting requires synchronization so we always do computeSync
        return new CountingGroupElement(
                group,
                (LazyGroupElement) elemTotal.computeSync(),
                (LazyGroupElement) elemExpMultiExp.computeSync()
        );
    }

    @Override
    public GroupElement computeSync() {
        return new CountingGroupElement(
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
        CountingGroupElement other = (CountingGroupElement) o;
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
