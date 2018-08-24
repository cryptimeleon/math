package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingTargetGroup;
import de.upb.crypto.math.pairings.generic.PairingTargetGroupElement;

/**
 * @see PairingTargetGroupElement
 */
public class SupersingularTargetGroupElement extends PairingTargetGroupElement {

    public SupersingularTargetGroupElement(PairingTargetGroup g, ExtensionFieldElement fe) {
        super(g, fe);
    }

}
