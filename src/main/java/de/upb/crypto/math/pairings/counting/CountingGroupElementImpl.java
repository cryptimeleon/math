package de.upb.crypto.math.pairings.counting;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * @see CountingGroupImpl
 */
public class CountingGroupElementImpl implements GroupElementImpl {
    protected Zn.ZnElement elem;
    protected CountingGroupImpl group;

    public CountingGroupElementImpl(CountingGroupImpl group, Zn.ZnElement elem) {
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
     * Helpful for the {@link CountingGroupImpl#multiexp(Multiexponentiation)} method.
     * @param count {@code true} if we want to count the operation, {@code false} otherwise
     */
    protected GroupElementImpl op(GroupElementImpl e, boolean count) {
        if (!(e instanceof CountingGroupElementImpl) || !((CountingGroupElementImpl) e).group.equals(group))
            throw new IllegalArgumentException("Incompatible groups. LHS: "
                    + group.name + "("
                    + group.size()
                    + "), RHS: "
                    + (e instanceof CountingGroupElementImpl ? ((CountingGroupElementImpl) e).group.name + "(" + ((CountingGroupElementImpl) e).group.size() + ")"
                    : e == null ? "null" : e.getStructure())
            );
        GroupElementImpl result = group.wrap(elem.add(((CountingGroupElementImpl) e).elem));
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
     * Helpful for the {@link CountingGroupImpl#multiexp(Multiexponentiation)} method.
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
        CountingGroupElementImpl that = (CountingGroupElementImpl) o;
        return Objects.equals(elem, that.elem) &&
                Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elem);
    }
}
