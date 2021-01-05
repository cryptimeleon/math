package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.GroupElement;

/**
 * Represents the result of a pairing evaluation.
 */
public class PairingResultLazyGroupElement extends LazyGroupElement {
    protected LazyGroupElement lhs, rhs;
    protected LazyBilinearMap bilMap;

    public PairingResultLazyGroupElement(LazyGroup gt, LazyBilinearMap bilMap, GroupElement lhs, GroupElement rhs) {
        super(gt);
        this.lhs = (LazyGroupElement) lhs;
        this.rhs = (LazyGroupElement) rhs;
        this.bilMap = bilMap;
    }

    @Override
    protected void computeConcreteValue() {
        setConcreteValue(bilMap.impl.apply(lhs.getConcreteValue(), rhs.getConcreteValue()));
        //TODO optimize: (1) draw exponents e(g,h)^x into e(g^x, h). (2) A product e(g,h)*e(g2,h2)*... can share the final exponentiation. (3) precomputation of pairings
    }
}
