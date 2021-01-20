package de.upb.crypto.math.structures.groups.elliptic.type1.supersingular;

import de.upb.crypto.math.structures.rings.extfield.ExtensionFieldElement;
import de.upb.crypto.math.structures.groups.elliptic.PairingTargetGroupElementImpl;
import de.upb.crypto.math.structures.groups.elliptic.PairingTargetGroupImpl;

/**
 * @see PairingTargetGroupElementImpl
 */
public class SupersingularTargetGroupElementImpl extends PairingTargetGroupElementImpl {

    public SupersingularTargetGroupElementImpl(PairingTargetGroupImpl g, ExtensionFieldElement fe) {
        super(g, fe);
    }
}
