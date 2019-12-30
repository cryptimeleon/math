package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.PairingSourceGroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.structures.ec.AbstractECPCoordinate;
import de.upb.crypto.math.structures.ec.EllipticCurvePoint;

public class SupersingularSourceGroupElement extends PairingSourceGroupElement {

    public SupersingularSourceGroupElement(WeierstrassCurve curve, FieldElement x, FieldElement y, FieldElement z) {
        super(curve, x, y, z);
    }

    public SupersingularSourceGroupElement(WeierstrassCurve curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    public SupersingularSourceGroupElement(WeierstrassCurve curve) {
        super(curve);
    }

    public SupersingularSourceGroupElement(AbstractECPCoordinate point) {
        super(point);
    }
}
