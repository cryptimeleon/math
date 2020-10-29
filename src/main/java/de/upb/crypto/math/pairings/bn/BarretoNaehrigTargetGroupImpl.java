package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingTargetGroupImpl;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;


public class BarretoNaehrigTargetGroupImpl extends PairingTargetGroupImpl {
    /**
     * Constructs a subgroup of size size in F12 where F12=F(v)=F[x]/(x^6+v).
     *
     * @param v    element that defines extension of degree 6
     * @param size size of subgroup
     */
    public BarretoNaehrigTargetGroupImpl(ExtensionFieldElement v, BigInteger size) {
        super(new ExtensionField(v, 6), size);
    }


    public BarretoNaehrigTargetGroupImpl(Representation r) {
        super(r);
    }

    @Override
    public BarretoNaehrigTargetGroupElementImpl getElement(ExtensionFieldElement fe) {
        return new BarretoNaehrigTargetGroupElementImpl(this, fe);
    }

    @Override
    public boolean hasPrimeSize() throws UnsupportedOperationException {
        return true;
    }

    @Override
    public double estimateCostInvPerOp() {
        return 614;
    }
}
