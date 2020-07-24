package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

import java.math.BigInteger;

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
    protected GroupElementImpl computeConcreteValue() {
        return bilMap.impl.apply(lhs.getConcreteGroupElement(), rhs.getConcreteGroupElement());
        //TODO optimize: (1) draw exponents e(g,h)^x into e(g^x, h) [maybe don't enforce this, but use the pow() override to auto-format it as such]. (2) A product e(g,h)*e(g2,h2)*... can share the final exponentiation. (3) precomputation of pairings
    }

    @Override
    public GroupElement pow(BigInteger exponent) {
        return new PairingResultLazyGroupElement(bilMap.gt, bilMap, lhs.pow(exponent), rhs); //TODO do I want to add a bunch more of these? Or just remove them completely?
    }

    @Override
    public GroupElement inv() {
        return new PairingResultLazyGroupElement(bilMap.gt, bilMap, lhs.inv(), rhs);
    }
}
