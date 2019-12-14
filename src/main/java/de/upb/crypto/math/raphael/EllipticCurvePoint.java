package de.upb.crypto.math.raphael;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.Representation;

public class EllipticCurvePoint implements GroupElement {

    public enum ECPCoordinateSystem {
        AFFINE, PROJECTIVE
    }

    private EllipticCurvePointCoordinates coords;

    public EllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y,
                              FieldElement z, ECPCoordinateSystem coordSystem) {
        switch (coordSystem) {
            case AFFINE:
                coords = new AffineEllipticCurvePointCoordinates(curve, x, y, z);
                break;
            case PROJECTIVE:
                coords = new ProjectiveEllipticCurvePointCoordinates(curve, x, y, z);
                break;
        }
    }

    public EllipticCurvePoint(EllipticCurvePointCoordinates coords) {
        this.coords = coords;
    }

    public FieldElement getX() {
        return coords.getX();
    }

    public FieldElement getY() {
        return coords.getY();
    }

    @Override
    public Group getStructure() {
        return coords.getStructure();
    }

    @Override
    public GroupElement inv() {
        return new EllipticCurvePoint(this.coords.inv());
    }

    @Override
    public GroupElement op(Element e) throws IllegalArgumentException {
        if (!(e instanceof EllipticCurvePoint)) {
            throw new IllegalArgumentException("Argument not EllipticCurvePoint.");
        }
        EllipticCurvePoint Q = (EllipticCurvePoint) e;
        // can even do mixed stuff with this if Q does not use same coordinate system.
        return new EllipticCurvePoint(this.coords.op(Q.coords));
    }

    @Override
    public boolean isNeutralElement() {
        return coords.isNeutralElement();
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return null;
    }

    @Override
    public Representation getRepresentation() {
        return null;
    }
}
