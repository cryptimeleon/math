package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.*;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;

import java.util.function.Function;

/**
 * Wrapper class for an elliptic curve point coordinate object. Allows subclassing
 * for curve/pairing-specific points with additional capabilities such as compression.
 *
 * @author Raphael Heitjohann
 */
public class EllipticCurvePoint implements GroupElement {

    /**
     * The actual point.
     */
    private AbstractECPCoordinate point;

    /**
     * Allows passing a constructor which takes in the curve and constructs a default element.
     * The specific point is then constructed afterwards. This allows using different coordinates
     * without having to use an enum.
     * @param curve
     * @param x
     * @param y
     * @param z
     */
    public EllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y, FieldElement z) {
        point = curve.getEcpCoordConstructor().apply(curve);
        point.setX(x);
        point.setY(y);
        point.setZ(z);
    }

    public EllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y) {
        point = curve.getEcpCoordConstructor().apply(curve);
        point.setX(x);
        point.setY(y);
    }

    public EllipticCurvePoint(WeierstrassCurve curve) {
        point = curve.getEcpCoordConstructor().apply(curve);
    }

    public EllipticCurvePoint(AbstractECPCoordinate point) {
        this.point = point;
    }

    public FieldElement getNormalizedX() {
        return point.getNormalizedX();
    }

    public FieldElement getNormalizedY() {
        return point.getNormalizedY();
    }

    /**
     * @return non-normalized x coordinate.
     */
    public FieldElement getX() {
        return point.x;
    }

    /**
     * @return non-normalized y coordinate.
     */
    public FieldElement getY() {
        return point.y;
    }

    /**
     * @return non-normalized z coordinate.
     */
    public FieldElement getZ() {
        return point.z;
    }

    public void setPoint(AbstractECPCoordinate point) {
        this.point = point;
    }

    public AbstractECPCoordinate getPoint() {
        return this.point;
    }

    @Override
    public Group getStructure() {
        return point.getStructure();
    }

    /**
     * @return Field of curve.
     */
    public Field getFieldOfDefinition() {
        return point.getFieldOfDefinition();
    }

    @Override
    public GroupElement inv() {
        return new EllipticCurvePoint(point.inv());
    }

    public FieldElement[] computeLine(EllipticCurvePoint Q) {
        return point.computeLine(Q.point);
    }

    public EllipticCurvePoint add(EllipticCurvePoint Q) {
        return new EllipticCurvePoint(point.add(Q.point));
    }

    public EllipticCurvePoint add(EllipticCurvePoint Q, FieldElement[] line) {
        return new EllipticCurvePoint(point.add(Q.point, line));
    }

    @Override
    public GroupElement op(Element e) throws IllegalArgumentException {
        EllipticCurvePoint P = (EllipticCurvePoint) e;

        return this.add(P);
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return point.updateAccumulator(accumulator);
    }

    @Override
    public Representation getRepresentation() {
        return point.getRepresentation();
    }

    @Override
    public boolean isNeutralElement() {
        return point.isNeutralElement();
    }

    public boolean isNormalized() {
        return point.isNormalized();
    }

    public EllipticCurvePoint normalize() {
        if (this.isNormalized()) {
            return this;
        }
        return new EllipticCurvePoint(point.normalize());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof EllipticCurvePoint)) {
            return false;
        }
        EllipticCurvePoint P = (EllipticCurvePoint) other;
        return point.equals(P.point);
    }

    @Override
    public String toString() {
        return point.toString();
    }

    @Override
    public int hashCode() {
        return point.hashCode();
    }
}
