package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;

public abstract class EllipticCurvePoint implements GroupElement {

    protected FieldElement x, y, z;
    protected WeierstrassCurve structure;


    public EllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y,
                              FieldElement z) {
        this.structure = curve;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public EllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y) {
        this(curve, x, y, curve.getFieldOfDefinition().getOneElement());
    }

    public EllipticCurvePoint(WeierstrassCurve curve) {
        // Init with neutral element
        this(curve,
                curve.getFieldOfDefinition().getZeroElement(),
                curve.getFieldOfDefinition().getOneElement(),
                curve.getFieldOfDefinition().getZeroElement()
        );
    }

    public FieldElement getX() {
        return normalize().x;
    }

    public FieldElement getY() {
        return normalize().y;
    }

    public FieldElement getZ() {
        return normalize().z;
    }

    public WeierstrassCurve getStructure() {
        return structure;
    }

    public Field getFieldOfDefinition() {
        return this.structure.getFieldOfDefinition();
    }

    public boolean isNeutralElement() {
        return z.isZero();
    }

    @Override
    public String toString() {
        return "(" + x.toString() + ":" + y.toString() + ":" + z.toString() + ")";
    }

    @Override
    public int hashCode() {
        if (isNormalized())
            return x.hashCode();
        else
            return normalize().x.hashCode(); //todo do something more efficient
    }

    @Override
    public Representation getRepresentation() {
        /*
         * normalize point to save memory for z-coordinates
         */
        EllipticCurvePoint normalized = this.normalize();
        ObjectRepresentation r = new ObjectRepresentation();
        r.put("x", normalized.x.getRepresentation());
        r.put("y", normalized.y.getRepresentation());
        r.put("z", normalized.z.getRepresentation()); //basically in this to represent the neutral element
        return r;
    }

    public boolean representsSamePoint(EllipticCurvePoint P) {
        return this.x.equals(P.x)
                && this.y.equals(P.y)
                && this.z.equals(P.z);
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        EllipticCurvePoint normalized = normalize();
        if (!getStructure().getFieldOfDefinition().getUniqueByteLength().isPresent()) {
            accumulator.escapeAndSeparate(normalized.x);
            accumulator.escapeAndSeparate(normalized.y);
            accumulator.escapeAndSeparate(normalized.z);
        } else {
            accumulator.append(normalized.x);
            accumulator.append(normalized.y);
            accumulator.append(normalized.z);
        }
        return accumulator;
    }

    public abstract EllipticCurvePoint normalize();

    public abstract boolean isNormalized();

    public abstract FieldElement[] computeLine(EllipticCurvePoint Q);

    @Override
    public abstract GroupElement op(Element e);

    public abstract EllipticCurvePoint add(EllipticCurvePoint P, FieldElement[] line);

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract GroupElement inv();

}
