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

    FieldElement getA6();

    FieldElement getA4();

    FieldElement getA3();

    FieldElement getA2();

    FieldElement getA1();

    EllipticCurvePoint getElement(FieldElement x, FieldElement y);

    EllipticCurvePoint getElement(AbstractECPCoordinate point);

    Class getCoordinateClass();

    /**
     * Allows cloning the structure with a new coordinate class to make coordinate class switching easier.
     * @param coordinateClass The new coordinate class.
     * @return The same weierstrass curve but with the given coordinate class.
     */
    //WeierstrassCurve withCoordinateClass(Class coordinateClass);

    default boolean isShortForm() {
        return getA3().isZero() && getA2().isZero() && getA1().isZero();
    }

}
