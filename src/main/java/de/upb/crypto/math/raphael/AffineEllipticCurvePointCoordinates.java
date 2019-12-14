package de.upb.crypto.math.raphael;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;

public class AffineEllipticCurvePointCoordinates extends EllipticCurvePointCoordinates {

    public AffineEllipticCurvePointCoordinates(WeierstrassCurve curve, FieldElement x,
                                               FieldElement y, FieldElement z) {
        super(curve, x, y, z);
    }

    public AffineEllipticCurvePointCoordinates(WeierstrassCurve curve, FieldElement x,
                                               FieldElement y) {
        super(curve, x, y);
    }

    @Override
    public EllipticCurvePointCoordinates op(EllipticCurvePointCoordinates Q) {
        return null;
    }

    @Override
    public EllipticCurvePointCoordinates inv() {
        return null;
    }
}
