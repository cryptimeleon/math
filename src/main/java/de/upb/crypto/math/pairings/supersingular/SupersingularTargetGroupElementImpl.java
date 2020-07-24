package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingTargetGroupImpl;
import de.upb.crypto.math.pairings.generic.PairingTargetGroupElementImpl;

/**
 * @see PairingTargetGroupElementImpl
 */
public class SupersingularTargetGroupElementImpl extends PairingTargetGroupElementImpl {

    public SupersingularTargetGroupElementImpl(PairingTargetGroupImpl g, ExtensionFieldElement fe) {
        super(g, fe);
    }

}
