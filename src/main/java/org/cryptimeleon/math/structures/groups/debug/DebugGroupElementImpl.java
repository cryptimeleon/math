package org.cryptimeleon.math.structures.groups.debug;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.GroupImpl;
import org.cryptimeleon.math.structures.groups.exp.Multiexponentiation;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * An element of {@link DebugGroupImpl} able to count group operations, inversions and squarings
 * it is involved with.
 * The actual group operations are realized via a Zn element.
 *
 * @see DebugGroupElement
 * @see DebugGroupImpl
 */
public class DebugGroupElementImpl implements GroupElementImpl {
    /**
     * The underlying Zn element that realizes the actual group operations.
     */
    protected Zn.ZnElement elem;

    /**
     * The group this element belongs to.
     */
    protected DebugGroupImpl group;

    /**
     * Initializes this element as belonging to the given group and using the given Zn element for its group operations.
     *
     * @param group The group this element belongs to.
     * @param elem The Zn element through which the group operations are done.
     */
    public DebugGroupElementImpl(DebugGroupImpl group, Zn.ZnElement elem) {
        this.elem = elem;
        this.group = group;
    }

    @Override
    public GroupImpl getStructure() {
        return group;
    }

    @Override
    public GroupElementImpl inv() {
        GroupElementImpl result = group.wrap(elem.neg());
        group.incrementNumInversions();
        return result;
    }

    /**
     * Allows to configure whether to count this operation.
     * Helpful for the {@link DebugGroupImpl#multiexp(Multiexponentiation)} method.
     * @param count {@code true} if we want to count the operation, {@code false} otherwise
     */
    protected GroupElementImpl op(GroupElementImpl e, boolean count) {
        if (!(e instanceof DebugGroupElementImpl) || !((DebugGroupElementImpl) e).group.equals(group))
            throw new IllegalArgumentException("Incompatible groups. LHS: "
                    + group.name + "("
                    + group.size()
                    + "), RHS: "
                    + (e instanceof DebugGroupElementImpl ? ((DebugGroupElementImpl) e).group.name + "(" + ((DebugGroupElementImpl) e).group.size() + ")"
                    : e == null ? "null" : e.getStructure())
            );
        GroupElementImpl result = group.wrap(elem.add(((DebugGroupElementImpl) e).elem));
        if (count) {
            if (this.equals(e)) {
                group.incrementNumSquarings();
            } else {
                group.incrementNumOps();
            }
        }
        return result;
    }

    @Override
    public GroupElementImpl op(GroupElementImpl e) throws IllegalArgumentException {
        return this.op(e, true);
    }

    /**
     * Allows to configure whether to count this exponentiation.
     * Helpful for the {@link DebugGroupImpl#multiexp(Multiexponentiation)} method.
     * @param k exponent
     * @param count {@code true} if we want to count the exponentiation, {@code false} otherwise
     */
    protected GroupElementImpl pow(BigInteger k, boolean count) {
        GroupElementImpl result = group.wrap(elem.mul(k));
        if (count)
            group.incrementNumExps();
        return result;
    }

    @Override
    public GroupElementImpl pow(BigInteger k) {
        return this.pow(k, true);
    }

    @Override
    public boolean isNeutralElement() {
        return elem.isZero();
    }

    @Override
    public String toString() {
        return group.name + "--" + elem.toString();
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return elem.updateAccumulator(accumulator);
    }

    @Override
    public Representation getRepresentation() {
        Representation result = elem.getRepresentation();
        group.incrementNumRetrievedRepresentations();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugGroupElementImpl that = (DebugGroupElementImpl) o;
        return Objects.equals(elem, that.elem) &&
                Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elem);
    }
}
