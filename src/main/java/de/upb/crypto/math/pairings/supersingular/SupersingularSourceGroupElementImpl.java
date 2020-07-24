package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.PairingSourceGroupElement;

public class SupersingularSourceGroupElementImpl extends PairingSourceGroupElement {

    public SupersingularSourceGroupElementImpl(SupersingularSourceGroupImpl curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    /**
     * Instantiates the neutral element
     *
     * @param curve
     */
    public SupersingularSourceGroupElementImpl(SupersingularSourceGroupImpl curve) {
        super(curve);
    }
}
