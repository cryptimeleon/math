package org.cryptimeleon.math.structures.groups.lazy;

import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.exp.MultiExpTerm;
import org.cryptimeleon.math.structures.groups.exp.Multiexponentiation;

import java.util.List;

/**
 * Represents the result of a group operation.
 */
class OpLazyGroupElement extends LazyGroupElement {
    LazyGroupElement lhs, rhs;
    GroupElementImpl accumulatedConstant = null;
    List<MultiExpTerm> terms = null;
    int firstTermIndex = -1, lastTermIndex = -1;

    public OpLazyGroupElement(LazyGroup group, LazyGroupElement lhs, LazyGroupElement rhs) {
        super(group);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    protected void computeConcreteValue() {
        Multiexponentiation multiexp = new Multiexponentiation();
        multiexp.put(this.accumulateMultiexp(multiexp)); //[sic!] adding the constant returned by accumulateMultiexp to the whole thing

        setConcreteValue(group.compute(multiexp));
    }

    @Override
    protected GroupElementImpl accumulateMultiexp(Multiexponentiation multiexp) {
        if (isComputed()) //we already know the exact value. Use that.
            return getConcreteValue();

        if (terms != null) { //accumulation was already computed earlier. Reusing those instead of descending into the children
            for (int i=firstTermIndex;i<=lastTermIndex;i++)
                multiexp.put(terms.get(i));
            return accumulatedConstant;
        }

        //Value is not yet cached. Accumulate it.
        firstTermIndex = multiexp.getNumberOfTerms();
        GroupElementImpl lhsConstant = lhs.isDefinitelySupposedToGetConcreteValue() ? lhs.getConcreteValue() : lhs.accumulateMultiexp(multiexp);
        GroupElementImpl rhsConstant = rhs.isDefinitelySupposedToGetConcreteValue() ? rhs.getConcreteValue() : rhs.accumulateMultiexp(multiexp);
        accumulatedConstant = lhsConstant == null ? rhsConstant : (rhsConstant == null ? lhsConstant : lhsConstant.op(rhsConstant));
        lastTermIndex = multiexp.getNumberOfTerms()-1;

        if (firstTermIndex <= lastTermIndex) //this value depends on the result of some multiexponentiation stuff.
            this.terms = multiexp.getTerms(); //cache it for later
        else if (accumulatedConstant != null)
            setConcreteValue(accumulatedConstant); //we haven't added anything to the multiexp. So we know the proper concrete value of this already.
        else
            setConcreteValue(group.impl.getNeutralElement());

        return accumulatedConstant;
    }
}
