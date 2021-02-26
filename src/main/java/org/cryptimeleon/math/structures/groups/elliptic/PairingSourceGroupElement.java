package org.cryptimeleon.math.structures.groups.elliptic;

import org.cryptimeleon.math.structures.rings.FieldElement;

public abstract class PairingSourceGroupElement extends AffineEllipticCurvePoint {

    //EllipticCurve structure;

    public PairingSourceGroupElement(PairingSourceGroupImpl curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    public PairingSourceGroupElement(PairingSourceGroupImpl curve) {
        super(curve);
    }
}
