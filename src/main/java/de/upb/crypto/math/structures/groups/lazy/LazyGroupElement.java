package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;
import de.upb.crypto.math.structures.groups.exp.SmallExponentPrecomputation;

import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class LazyGroupElement implements GroupElement {
    protected LazyGroup group;
    protected volatile Future<GroupElementImpl> concreteValue;
    protected SmallExponentPrecomputation precomputedSmallExponents = null;

    public LazyGroupElement(LazyGroup group) {
        this.group = group;
    }

    public LazyGroupElement(LazyGroup group, GroupElementImpl concreteValue) {
        this(group);
        this.concreteValue = CompletableFuture.completedFuture(concreteValue);
    }

    @Override
    public Group getStructure() {
        return group;
    }

    @Override
    public GroupElement inv() {
        return new InvLazyGroupElement(group, this);
    }

    @Override
    public GroupElement op(Element e) throws IllegalArgumentException {
        return new OpLazyGroupElement(group, this, (LazyGroupElement) e);
    }

    @Override
    public GroupElement square() {
        return new OpLazyGroupElement(group, this, this);
    } //TODO

    @Override
    public GroupElement pow(BigInteger exponent) {
        return new ExpLazyGroupElement(group, this, exponent);
    }

    @Override
    public GroupElement precomputePow() {
        if (group.precomputationWindowSize > 0)
            precomputedSmallExponents = new SmallExponentPrecomputation(getConcreteGroupElement(), group.precomputationWindowSize);
        return this;
    }

    @Override
    public GroupElement compute() {
        if (concreteValue == null) {
            concreteValue = LazyGroup.executor.submit(this::computeConcreteValue);
        }
        return this;
    }

    @Override
    public GroupElement computeSync() {
        if (concreteValue == null) {
            concreteValue = new CompletableFuture<>();
            ((CompletableFuture<GroupElementImpl>) concreteValue).complete(computeConcreteValue());
        }

        return this;
    }

    protected void announceSettingConcreteValue() {
        concreteValue = new CompletableFuture<>();
    }

    protected void setConcreteValue(GroupElementImpl impl) {
        if (concreteValue == null) {
            concreteValue = CompletableFuture.completedFuture(impl);
        }
    }

    /**
     * Computes the concrete value of the expression.
     * Implementations should call getConcreteGroupElement() on
     * dependent LazyGroupElements.
     *
     * May be called multiple times (in several threads), but best effort is applied to only call once.
     * So if the value this computes is not constant (e.g., is random), you should apply synchronization here
     * and make sure you're always returning a consistent value here.
     */
    protected abstract GroupElementImpl computeConcreteValue();

    /**
     * Subclasses shall call put() on the multiexponentiation with the equivalent to computeConcreteValue()
     */
    protected void accumulateMultiexp(Multiexponentiation multiexp) {
        multiexp.put(computeConcreteValue());
    }

    public GroupElementImpl getConcreteGroupElement() {
        if (concreteValue == null)
            computeSync();

        try {
            return concreteValue.get();
        } catch (InterruptedException e) {
            System.err.println("Thread waiting for group element got interrupted.");
            return computeConcreteValue();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public boolean isComputed() {
        return concreteValue != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LazyGroupElement)) return false;
        LazyGroupElement that = (LazyGroupElement) o;
        if (!group.equals(that.group)) return false;
        if (!isComputed() && !that.isComputed())
            return op(that.inv()).isNeutralElement();
        return getConcreteGroupElement().equals(that.getConcreteGroupElement());
    }

    @Override
    public boolean isNeutralElement() {
        return getConcreteGroupElement().isNeutralElement();
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, getConcreteGroupElement());
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        getConcreteGroupElement().updateAccumulator(accumulator);
        return accumulator;
    }

    @Override
    public Representation getRepresentation() {
        return getConcreteGroupElement().getRepresentation();
    }
}
