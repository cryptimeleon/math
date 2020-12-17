package de.upb.crypto.math.pairings.type3.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

public class BarretoNaehrigGroup1Impl extends BarretoNaehrigSourceGroupImpl {
    /**
     * Construct subgroup of E:y^2=x^3+a6 with assumed size and cofactor.
     *
     * @param size
     * @param cofactor
     * @param a6
     */
    public BarretoNaehrigGroup1Impl(BigInteger size, BigInteger cofactor, ExtensionFieldElement a6) {
        super(size, cofactor, a6);
    }

    /**
     * standard constructor for StandaloneRepresentation
     **/
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
