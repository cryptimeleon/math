package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.interfaces.structures.FieldElement;

public class SupersingularSourceGroupElement extends AffineEllipticCurvePoint {

    public SupersingularSourceGroupElement(SupersingularSourceGroup curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    /**
     * Instantiates the neutral element
     *
     * @param curve
     */
    public SupersingularSourceGroupElement(SupersingularSourceGroup curve) {
        super(curve);
    }
}
