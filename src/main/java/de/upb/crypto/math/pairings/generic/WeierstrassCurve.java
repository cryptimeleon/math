package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.structures.EllipticCurve;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.ec.AbstractECPCoordinate;
import de.upb.crypto.math.structures.ec.EllipticCurvePoint;
import de.upb.crypto.math.interfaces.structures.FieldElement;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * The set of points (x,y) such that
 * <p>
 * y^2+A1*x*y + A3*y = x^3 + A2*x^2 + A4*x + A6
 */
public interface WeierstrassCurve extends EllipticCurve {

    public FieldElement getA6();

    public FieldElement getA4();

    public FieldElement getA3();

    public FieldElement getA2();

    public FieldElement getA1();

    public EllipticCurvePoint getElement(FieldElement x, FieldElement y);

    public EllipticCurvePoint getElement(AbstractECPCoordinate point);

    public Class getCoordinateClass();

    public default boolean isShortForm() {
        return getA3().isZero() && getA2().isZero() && getA1().isZero();
    }

}
