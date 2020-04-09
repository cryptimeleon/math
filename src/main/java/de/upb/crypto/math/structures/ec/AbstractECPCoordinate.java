package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;

public abstract class AbstractECPCoordinate {

    protected FieldElement x, y, z;
    protected WeierstrassCurve structure;


    public AbstractECPCoordinate(WeierstrassCurve curve, FieldElement x, FieldElement y,
                              FieldElement z) {
        this.structure = curve;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public AbstractECPCoordinate(WeierstrassCurve curve, FieldElement x, FieldElement y) {
        this(curve, x, y, curve.getFieldOfDefinition().getOneElement());
    }

    public AbstractECPCoordinate(WeierstrassCurve curve) {
        // Init with neutral element
        this(curve,
                curve.getFieldOfDefinition().getZeroElement(),
                curve.getFieldOfDefinition().getOneElement(),
                curve.getFieldOfDefinition().getZeroElement()
        );
    }

    public FieldElement getNormalizedX() {
        return normalize().x;
    }

    public FieldElement getNormalizedY() {
        return normalize().y;
    }

    public FieldElement getNormalizedZ() {
        return normalize().z;
    }

    public void setX(FieldElement x) { this.x = x; }

    public void setY(FieldElement y) { this.y = y; }

    public void setZ(FieldElement z) { this.z = z; }

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
        return this.getClass().getSimpleName() + "(" + x.toString() + ":" + y.toString() + ":" + z.toString() + ")";
    }

    @Override
    public int hashCode() {
        if (isNormalized())
            return x.hashCode();
        else
            return normalize().x.hashCode(); //todo do something more efficient
    }

    public Representation getRepresentation() {
        /*
         * normalize point to save memory for z-coordinates
         */
        AbstractECPCoordinate normalized = this.normalize();
        ObjectRepresentation r = new ObjectRepresentation();
        r.put("x", normalized.x.getRepresentation());
        r.put("y", normalized.y.getRepresentation());
        r.put("z", normalized.z.getRepresentation()); //basically in this to represent the neutral element
        return r;
    }

    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        AbstractECPCoordinate normalized = normalize();
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

    public abstract AbstractECPCoordinate normalize();

    public abstract boolean isNormalized();

    public abstract FieldElement[] computeLine(AbstractECPCoordinate Q);

    public abstract AbstractECPCoordinate add(AbstractECPCoordinate P);

    public abstract AbstractECPCoordinate add(AbstractECPCoordinate P, FieldElement[] line);

    @Override
    public abstract boolean equals(Object other);

    public abstract AbstractECPCoordinate inv();

}
