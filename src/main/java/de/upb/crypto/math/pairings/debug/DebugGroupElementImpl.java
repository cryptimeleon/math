package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * @see DebugGroupImpl
 */
public class DebugGroupElementImpl implements GroupElementImpl {
    protected Zn.ZnElement elem;
    protected DebugGroupImpl group;

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
        //DebugGroupLogger.log(group.name, "inv");
        GroupElementImpl result = group.wrap(elem.neg());
        group.incrementNumInversions();
        return result;
    }

    /**
     * Allows to configure whether to count this operation.
     * Helpful for the {@link DebugGroupImpl#multiexp(Multiexponentiation)} method.
     * @param k exponent
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
        return elem.getRepresentation();
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
