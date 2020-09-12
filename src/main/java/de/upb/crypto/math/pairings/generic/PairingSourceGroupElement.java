package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.structures.ec.AffineEllipticCurvePoint;

public abstract class PairingSourceGroupElement extends AffineEllipticCurvePoint {

    //EllipticCurve structure;

    public PairingSourceGroupElement(PairingSourceGroupImpl curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    public PairingSourceGroupElement(PairingSourceGroupImpl curve) {
        super(curve);
    }
}
