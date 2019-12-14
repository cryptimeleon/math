package de.upb.crypto.math.raphael;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;

public abstract class EllipticCurvePointCoordinates {

    private FieldElement x, y, z;
    private WeierstrassCurve structure;

    public EllipticCurvePointCoordinates(WeierstrassCurve curve, FieldElement x, FieldElement y,
                                         FieldElement z) {
        this.structure = curve;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public EllipticCurvePointCoordinates(WeierstrassCurve curve, FieldElement x, FieldElement y) {
        this(curve, x, y, curve.getFieldOfDefinition().getOneElement());
    }

    public FieldElement getX() {
        return x;
    }

    public FieldElement getY() {
        return y;
    }

    private FieldElement getZ() {
        return z;
    }

    public Group getStructure() {
        return structure;
    }

    public abstract EllipticCurvePointCoordinates op(EllipticCurvePointCoordinates Q);

    public abstract EllipticCurvePointCoordinates inv();

    public boolean isNeutralElement() {
        return z.isZero();
    }
}
