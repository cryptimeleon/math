package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.structures.EllipticCurve;
import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.interfaces.structures.FieldElement;

/**
 * An elliptic curve defined by the weierstrass equation y^2 + A1*x*y + A3*y = x^3 + A2*x^2 + A4*x + A6.
 * <p>
 * Contains the set of points (x,y) that fulfill the weierstrass equation.
 * In short form, the equation reduces to y^2 = x^3 + A4*x + A6.
 */
public interface WeierstrassCurve extends EllipticCurve {


    public FieldElement getA6();

    public FieldElement getA4();

    public FieldElement getA3();

    public FieldElement getA2();

    public FieldElement getA1();

    public EllipticCurvePoint getElement(FieldElement x, FieldElement y);

    public default boolean isShortForm() {
        return getA3().isZero() && getA2().isZero() && getA1().isZero();
    }

}
