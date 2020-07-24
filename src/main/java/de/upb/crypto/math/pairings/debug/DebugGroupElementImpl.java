package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.serialization.Representation;
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
        return group.wrap(elem.neg());
    }

    @Override
    public GroupElementImpl op(GroupElementImpl e) throws IllegalArgumentException {
        if (!(e instanceof DebugGroupElementImpl) || !((DebugGroupElementImpl) e).group.equals(group))
            throw new IllegalArgumentException("Incompatible groups. LHS: "
                    + group.name + "("
                    + group.size()
                    + "), RHS: "
                    + (e instanceof DebugGroupElementImpl ? ((DebugGroupElementImpl) e).group.name + "(" + ((DebugGroupElementImpl) e).group.size() + ")"
                    : e == null ? "null" : e.getStructure())
            );
        
        return group.wrap(elem.add(((DebugGroupElementImpl) e).elem));
    }

    @Override
    public GroupElementImpl pow(BigInteger k) {
        return group.wrap(elem.mul(k));
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
