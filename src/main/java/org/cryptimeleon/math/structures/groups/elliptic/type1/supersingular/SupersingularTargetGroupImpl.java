package org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.elliptic.PairingTargetGroupElementImpl;
import org.cryptimeleon.math.structures.groups.elliptic.PairingTargetGroupImpl;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionField;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionFieldElement;

import java.math.BigInteger;

/**
 * @see PairingTargetGroupImpl
 */
public class SupersingularTargetGroupImpl extends PairingTargetGroupImpl {

    public SupersingularTargetGroupImpl(ExtensionField f, BigInteger size) {
        super(f, size);
    }

    public SupersingularTargetGroupImpl(Representation r) {
        super(r);
    }


    @Override
    public PairingTargetGroupElementImpl getElement(ExtensionFieldElement fe) {
        return new SupersingularTargetGroupElementImpl(this, fe);
    }

    @Override
    public boolean hasPrimeSize() {
        return true;
    }

    @Override
    public double estimateCostInvPerOp() {
        return 275;
    }
}
