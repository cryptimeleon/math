package org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular;

import org.cryptimeleon.math.structures.groups.elliptic.PairingTargetGroupElementImpl;
import org.cryptimeleon.math.structures.groups.elliptic.PairingTargetGroupImpl;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionFieldElement;

/**
 * @see PairingTargetGroupElementImpl
 */
public class SupersingularTargetGroupElementImpl extends PairingTargetGroupElementImpl {

    public SupersingularTargetGroupElementImpl(PairingTargetGroupImpl g, ExtensionFieldElement fe) {
        super(g, fe);
    }
}
