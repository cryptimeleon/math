package org.cryptimeleon.math.structures.groups.elliptic.type3.bn;

import org.cryptimeleon.math.structures.groups.elliptic.PairingTargetGroupElementImpl;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionFieldElement;

import java.math.BigInteger;

/**
 * Element of target group GT.
 */
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
