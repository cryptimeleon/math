package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingTargetGroup;
import de.upb.crypto.math.pairings.generic.PairingTargetGroupElement;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

/**
 * @see PairingTargetGroup
 */
public class SupersingularTargetGroup extends PairingTargetGroup {

    public SupersingularTargetGroup(ExtensionField f, BigInteger size) {
        super(f, size);
    }

    public SupersingularTargetGroup(Representation r) {
        super(r);
    }


    @Override
    public PairingTargetGroupElement getElement(ExtensionFieldElement fe) {
        return new SupersingularTargetGroupElement(this, fe);
    }

}
