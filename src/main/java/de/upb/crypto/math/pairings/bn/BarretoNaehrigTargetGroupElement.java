package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingTargetGroupElement;

import java.math.BigInteger;

public class BarretoNaehrigTargetGroupElement extends PairingTargetGroupElement {
    public BarretoNaehrigTargetGroupElement(BarretoNaehrigTargetGroup g, ExtensionFieldElement fe) {
        super(g, fe);
    }

    @Override
    public BarretoNaehrigTargetGroupElement pow(BigInteger e) {
        return (BarretoNaehrigTargetGroupElement) super.pow(e);
    }

    @Override
    public BarretoNaehrigTargetGroup getStructure() {
        return (BarretoNaehrigTargetGroup) super.getStructure();
    }
}
