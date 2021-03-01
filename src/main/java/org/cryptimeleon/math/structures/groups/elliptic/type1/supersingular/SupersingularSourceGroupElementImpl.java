package org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular;

import org.cryptimeleon.math.structures.groups.elliptic.PairingSourceGroupElement;
import org.cryptimeleon.math.structures.rings.FieldElement;

/**
 * An element of the source group (G1 and G2) of the supersingular pairing.
 *
 * @see SupersingularSourceGroupImpl
 */
public class SupersingularSourceGroupElementImpl extends PairingSourceGroupElement {

    public SupersingularSourceGroupElementImpl(SupersingularSourceGroupImpl curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    /**
     * Instantiates the neutral element
     *
     * @param curve the source curve the element should be on
     */
    public SupersingularSourceGroupElementImpl(SupersingularSourceGroupImpl curve) {
        super(curve);
    }
}
