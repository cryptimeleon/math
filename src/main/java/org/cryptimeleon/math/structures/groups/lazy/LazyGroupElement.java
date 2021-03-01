package org.cryptimeleon.math.structures.groups.lazy;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.Element;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.exp.Multiexponentiation;
import org.cryptimeleon.math.structures.groups.exp.SmallExponentPrecomputation;

import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Abstract class providing the base for different lazy group operation results.
 */
public abstract class LazyGroupElement implements GroupElement {
    protected LazyGroup group;
    private GroupElementImpl concreteValue = null;
    private volatile ComputationState computationState = ComputationState.NOTHING;
    private CompletableFuture<GroupElement> futureConcreteValue = null;
    private SmallExponentPrecomputation precomputedSmallExponents = null;

    protected enum ComputationState {
        /**
         * Nothing specified, we likely will never have to compute the concrete value of this.
         */
        NOTHING,
        /**
         * Someone called compute(), so this will eventually be computed, but that process hasn't started yet (it's queued).
         */
        REQUESTED,
        /**
         * Computation has begun. Meaning that it will eventually finish (in the sense that it's impossible to deadlock).
         */
        IN_PROGRESS,
        /**
         * Concrete value has been computed, i.e. concreteValue != null
         */
        DONE
    }

    public LazyGroupElement(LazyGroup group) {
        this.group = group;
    }

    public LazyGroupElement(LazyGroup group, GroupElementImpl concreteValue) {
        this(group);
        setConcreteValue(concreteValue);
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
        if (!(e instanceof LazyGroupElement) || !((LazyGroupElement) e).group.equals(this.group))
            throw new IllegalArgumentException("Groups don't match: "+group.toString()+" vs "+e.getStructure().toString());
        return new OpLazyGroupElement(group, this, (LazyGroupElement) e);
    }

    @Override
    public GroupElement square() {
        return new OpLazyGroupElement(group, this, this);
    }

    @Override
    public GroupElement pow(BigInteger exponent) {
        return new ExpLazyGroupElement(group, this, exponent);
    }

    @Override
    public GroupElement precomputePow() {
        return precomputePow(group.precomputationWindowSize);
    }

    @Override
    public GroupElement precomputePow(int windowSize) {
        if (windowSize > 0) {
            getPrecomputedSmallExponents().compute(
                    windowSize, getConcreteValue().getStructure().estimateCostInvPerOp() > 1
            );
            getPrecomputedSmallExponents().computeNegativePowers(
                    windowSize, getConcreteValue().getStructure().estimateCostInvPerOp() > 1
            );
        }
        return this;
    }

    @Override
    public GroupElement compute() {
        if (computationState == ComputationState.NOTHING) {
            computationState = ComputationState.REQUESTED;
            LazyGroup.executor.submit(this::computeSync); //this computeSync() call may theoretically end up not doing anything because another thread may already have computed the result (or started to).
        }
        return this;
    }

    @Override
    public GroupElement computeSync() {
        getConcreteValue();
        return this;
    }

    protected void setConcreteValue(GroupElementImpl impl) {
        concreteValue = impl;
        computationState = ComputationState.DONE;
    }

    /**
     * Returns the concrete group element behind this LazyGroupElement.
     * If it has already been computed, the cached value is returned.
     * If it is in the process of being computed, this blocks until the value is ready.
     */
    protected GroupElementImpl getConcreteValue() {
        if (computationState == ComputationState.IN_PROGRESS) { //someone else is already computing this. We'll just wait for that to finish.
            try {
                futureConcreteValue.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else if (computationState != ComputationState.DONE) { //there's something for us to do
            //Note on concurrency: potentially multiple threads may (probably very rarely) reach this stage. But that's fine, both would just compute the same value.
            futureConcreteValue = new CompletableFuture<>(); //set up Future for other threads to wait on if they need the value we're going to compute
            computationState = ComputationState.IN_PROGRESS; //mark computation IN_PROGRESS. Because computationState is volatile, if any thread reads this state, the futureConcreteValue is also already set.
            computeConcreteValue(); //actually compute the value of this LazyGroupElement. Goal for this call is to run setConcreteValue().
            // This may block for some time if it depends on some value that's also already IN_PROGRESS (but there is no way this results in a deadlock because of the non-cyclic nature of these computations).
            futureConcreteValue.complete(this); //wake up anyone waiting for us to finish.
        }

        return concreteValue;
    }

    /**
     * Generally, when computing the value of some LazyGroupElement, there is no need to compute the values of all LazyGroupElements
     * related to it on the way. One example of this is multiexponentiation, where the element g^a * h^b generally should be computed
     * without explicitly computing g^a and h^b.
     *
     * However, if this returns true, then we have indication that at some point, we'll want to have the concrete value of this LazyGroupElement,
     * i.e. if g^a.isDefinitelySupposedToGetConcreteValue(), then it's more advantageous to actually compute g^a instead of "just" including it in the multiexponentiation.
     *
     */
    protected boolean isDefinitelySupposedToGetConcreteValue() {
        return computationState != ComputationState.NOTHING;
    }

    /**
     * Computes the concrete value of the expression. Goal is to call setConcreteValue().
     * Implementations should call getConcreteValue() on dependent LazyGroupElements if needed.
     *
     * May be called multiple times (in several threads), but generally, best effort is applied to only call once.
     * So if the value this computes is not constant (e.g., is random), you should apply synchronization here
     * and make sure you're always returning a consistent value here.
     */
    protected abstract void computeConcreteValue();

    /**
     * Writes down the value of this group element as a multiexponentiation.
     * Implementors shall return a value h and put values g_i^x_i into multiexp such that
     * h * product(g_i^x_i) is the value of this LazyGroupElement.
     * For h = 1, return null.
     *
     * Yes, this is slightly weird from an API design perspective, but it's for the sake of
     * (probably premature) optimization.
     */
    protected GroupElementImpl accumulateMultiexp(Multiexponentiation multiexp) {
        return getConcreteValue(); //subclasses shall overwrite if they have better ideas than this naive way.
    }

    public SmallExponentPrecomputation getPrecomputedSmallExponents() {
        if (precomputedSmallExponents == null)
            precomputedSmallExponents = new SmallExponentPrecomputation(getConcreteValue());
        return precomputedSmallExponents;
    }

    @Override
    public boolean isComputed() {
        return computationState == ComputationState.DONE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // this equals should work between lazy group elements with the same value, e.g. random element and constant
        if (!(o instanceof LazyGroupElement)) return false;
        LazyGroupElement that = (LazyGroupElement) o;
        if (!group.equals(that.group)) return false;
        return getConcreteValue().equals(that.getConcreteValue());
    }

    @Override
    public boolean isNeutralElement() {
        return getConcreteValue().isNeutralElement();
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, getConcreteValue());
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        getConcreteValue().updateAccumulator(accumulator);
        return accumulator;
    }

    @Override
    public Representation getRepresentation() {
        return getConcreteValue().getRepresentation();
    }

    @Override
    public String toString() {
        if (computationState == ComputationState.DONE)
            return concreteValue.toString();
        return "LazyGroupElement{" +
                "computationState=" + computationState +
                '}';
    }
}
