package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.*;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;

import java.lang.reflect.InvocationTargetException;
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

    public EllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y, FieldElement z) {
        try {
            point = (AbstractECPCoordinate) curve.getCoordinateClass()
                    .getConstructor(WeierstrassCurve.class, FieldElement.class, FieldElement.class, FieldElement.class)
                    .newInstance(curve, x, y, z);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Given coordinate class '" + curve.getCoordinateClass() +
                    "' does not have a constructor taking in a Weierstrass Curve and three field elements.");
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException("An error occurred instantiating the element from the representation.");
        }
    }

    public EllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y) {
        this(curve, x, y, curve.getFieldOfDefinition().getOneElement());
    }

    public EllipticCurvePoint(WeierstrassCurve curve) {
        this(curve,
                curve.getFieldOfDefinition().getZeroElement(),
                curve.getFieldOfDefinition().getOneElement(),
                curve.getFieldOfDefinition().getZeroElement()
        );
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

    public WeierstrassCurve getStructure() {
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
        return this.getStructure().getElement(point.inv());
    }

    /**
     * Compute line through this point and the given point.
     * @param Q second point line should go through in addition to this.
     * @return  array containing two coefficients of the linear combination describing the line.
     */
    public FieldElement[] computeLine(EllipticCurvePoint Q) {
        return point.computeLine(Q.point);
    }

    /**
     * Add two points.
     * @param Q point to add.
     * @return points added together as a new point.
     */
    public EllipticCurvePoint add(EllipticCurvePoint Q) {
        return this.getStructure().getElement(point.add(Q.point));
    }

    /**
     * Add two points using line through them as help. Line is only used in affine coordinates.
     * @param Q    point to add.
     * @param line array of two coefficients describing line.
     * @return     points added together as a new point.
     */
    public EllipticCurvePoint add(EllipticCurvePoint Q, FieldElement[] line) {
        // getStructure call is important so we get a proper element of the structure and not just an EllipticCurvePoint
        // which we cannot cast down
        return this.getStructure().getElement(point.add(Q.point, line));
    }

    /**
     * Implements group operation. Calls add internally.
     * @param e right hand side of the operation
     * @return  points added together as new point.
     * @throws IllegalArgumentException
     */
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
        return this.getStructure().getElement(point.normalize());
    }

    /**
     * Checks if two points are equals. Normalizes points first if necessary for the coordinate system.
     * Checks that the points use the same coordinate representation as well.
     * @param other
     * @return
     */
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
