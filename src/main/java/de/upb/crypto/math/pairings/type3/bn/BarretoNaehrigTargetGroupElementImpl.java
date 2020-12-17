package de.upb.crypto.math.pairings.type3.bn;

import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingTargetGroupElementImpl;

import java.math.BigInteger;

public class BarretoNaehrigTargetGroupElementImpl extends PairingTargetGroupElementImpl {
    public BarretoNaehrigTargetGroupElementImpl(BarretoNaehrigTargetGroupImpl g, ExtensionFieldElement fe) {
        super(g, fe);
    }

    @Override
    public BarretoNaehrigTargetGroupElementImpl pow(BigInteger e) {
        return (BarretoNaehrigTargetGroupElementImpl) super.pow(e);
    }

    @Override
    public BarretoNaehrigTargetGroupImpl getStructure() {
        return (BarretoNaehrigTargetGroupImpl) super.getStructure();
    }
}
