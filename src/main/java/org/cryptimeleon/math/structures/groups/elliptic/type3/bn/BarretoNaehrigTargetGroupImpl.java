package org.cryptimeleon.math.structures.groups.elliptic.type3.bn;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.elliptic.PairingTargetGroupImpl;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionField;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionFieldElement;

import java.math.BigInteger;


/**
 * Target group GT.
 */
class BarretoNaehrigTargetGroupImpl extends PairingTargetGroupImpl {
    /**
     * Constructs a subgroup of given size in F12 where F12=F(v)=F[x]/(x^6+v).
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
