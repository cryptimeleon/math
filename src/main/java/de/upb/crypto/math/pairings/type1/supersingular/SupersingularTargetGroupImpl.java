package de.upb.crypto.math.pairings.type1.supersingular;

import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingTargetGroupImpl;
import de.upb.crypto.math.pairings.generic.PairingTargetGroupElementImpl;
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
