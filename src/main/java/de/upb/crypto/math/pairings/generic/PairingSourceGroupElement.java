package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.structures.ec.AbstractECPCoordinate;
import de.upb.crypto.math.structures.ec.EllipticCurvePoint;

public class PairingSourceGroupElement extends EllipticCurvePoint {

    public PairingSourceGroupElement(WeierstrassCurve curve, FieldElement x, FieldElement y, FieldElement z) {
        super(curve, x, y, z);
    }

    public PairingSourceGroupElement(WeierstrassCurve curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    public PairingSourceGroupElement(WeierstrassCurve curve) {
        super(curve);
    }

    public PairingSourceGroupElement(AbstractECPCoordinate point) {
        super(point);
    }
}
