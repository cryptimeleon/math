package de.upb.crypto.math.pairings.type3.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

/**
 * G1 in the Barreto-Naehrig bilinear group.
 */
public class BarretoNaehrigGroup1Impl extends BarretoNaehrigSourceGroupImpl {
    /**
     * Construct subgroup of E:y^2=x^3+a6 using given parameters.
     *
     * @param size size of the subgroup
     * @param cofactor the cofactor, i.e. the size of the base field divided by the size of the subgroup
     * @param a6 curve parameter for weierstrass equation
     */
    public BarretoNaehrigGroup1Impl(BigInteger size, BigInteger cofactor, ExtensionFieldElement a6) {
        super(size, cofactor, a6);
    }

    /**
     * Recreates a group from the given representation.
     *
     * @param r the representation to use for reconstruction
     */
    public BarretoNaehrigGroup1Impl(Representation r) {
        super(r);
    }

    private BigInteger traceFrobenius() {
        // t=q-E(F_q)+1 = q-r+1
        return this.getFieldOfDefinition().size().subtract(this.size()).add(BigInteger.ONE);
    }

    @Override
    public BarretoNaehrigGroup1ElementImpl getNeutralElement() {
        return new BarretoNaehrigGroup1ElementImpl(this);
    }

    @Override
    public BarretoNaehrigGroup1ElementImpl getElement(FieldElement x, FieldElement y) {
        return new BarretoNaehrigGroup1ElementImpl(this, x, y);
    }
  
    @Override
    public double estimateCostInvPerOp() {
        return 307;
    }
}
