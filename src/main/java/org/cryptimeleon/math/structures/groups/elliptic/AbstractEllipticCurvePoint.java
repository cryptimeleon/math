package org.cryptimeleon.math.structures.groups.elliptic;

import org.cryptimeleon.math.serialization.ObjectRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.rings.Field;
import org.cryptimeleon.math.structures.rings.FieldElement;

public abstract class AbstractEllipticCurvePoint implements EllipticCurvePoint {
    FieldElement x, y, z;

    WeierstrassCurve structure;

    public AbstractEllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y, FieldElement z) {
        this.structure = curve;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Deserializes a curve element.
     *
     * @throws IllegalArgumentException if the point is not on the curve.
     */
    public AbstractEllipticCurvePoint(WeierstrassCurve curve, Representation repr) {
        // first we have to instantiate the point so we can normalize
        FieldElement x = curve.getFieldOfDefinition().restoreElement(repr.obj().get("x"));
        FieldElement y = curve.getFieldOfDefinition().restoreElement(repr.obj().get("y"));
        FieldElement z = curve.getFieldOfDefinition().restoreElement(repr.obj().get("z"));
        // check whether z is zero (neutral element) first so we can potentially save curve check
        if (!z.isZero() && !curve.isOnCurve(x, y)) {
            throw new IllegalArgumentException("Point cannot be deserialized as it is not on the given curve");
        }
        this.structure = curve;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Field getFieldOfDefinition() {
        return structure.getFieldOfDefinition();
    }

    public FieldElement getX() {
        return x;
    }

    public FieldElement getY() {
        return y;
    }

    public FieldElement getZ() {
        return z;
    }

    public WeierstrassCurve getStructure() {
        return structure;
    }

    @Override
    public Representation getRepresentation() {
        /*
         * normalize point to save memory for z-coordinates and to avoid information leaks
         */
        AbstractEllipticCurvePoint normalized = (AbstractEllipticCurvePoint) this.normalize();
        ObjectRepresentation r = new ObjectRepresentation();
        r.put("x", normalized.getX().getRepresentation());
        r.put("y", normalized.getY().getRepresentation());
        r.put("z", normalized.getZ().getRepresentation()); //basically in this to represent the neutral element
        return r;
    }

    public String toString() {
        return isNeutralElement() ? "point at infinity" :
                z.isOne() ? "(" + x.toString() + "," + y.toString() + ")" :
                        "(" + x.toString() + "," + y.toString() + ", "+ z.toString() +")";
    }

    @Override
    public int hashCode() {
        if (isNormalized())
            return getX().hashCode();
        else
            return ((AbstractEllipticCurvePoint) normalize()).getX().hashCode(); //todo do something more efficient
    }

    @Override
    public boolean isNeutralElement() {
        return z.isZero();
    }
}
