package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.interfaces.structures.AbstractGroupElement;
import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;

public abstract class AbstractEllipticCurvePoint implements EllipticCurvePoint {
    FieldElement x, y, z;

    WeierstrassCurve structure;

    public AbstractEllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y, FieldElement z) {
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
         * normalize point to save memory for z-coordinates
         */
        AbstractEllipticCurvePoint normalized = (AbstractEllipticCurvePoint) this.normalize();
        ObjectRepresentation r = new ObjectRepresentation();
        r.put("x", normalized.getX().getRepresentation());
        r.put("y", normalized.getY().getRepresentation());
        r.put("z", normalized.getZ().getRepresentation()); //basically in this to represent the neutral element
        return r;
    }

    public String toString() {
        return "(" + x.toString() + ":" + y.toString() + ":" + z.toString() + ")";
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
