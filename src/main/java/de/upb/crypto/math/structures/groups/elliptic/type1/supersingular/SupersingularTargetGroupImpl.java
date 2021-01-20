package de.upb.crypto.math.structures.groups.elliptic.type1.supersingular;

import de.upb.crypto.math.structures.rings.extfield.ExtensionField;
import de.upb.crypto.math.structures.rings.extfield.ExtensionFieldElement;
import de.upb.crypto.math.structures.groups.elliptic.PairingTargetGroupElementImpl;
import de.upb.crypto.math.structures.groups.elliptic.PairingTargetGroupImpl;
import de.upb.crypto.math.serialization.Representation;

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
