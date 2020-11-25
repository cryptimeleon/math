package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.structures.EllipticCurve;
import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.interfaces.structures.FieldElement;

/**
 * An elliptic curve defined by the weierstrass equation
 * \(y^2 + A1 \cdot xy + A3 \cdot y = x^3 + A2 \cdot x^2 + A4 \cdot x + A6\).
 * <p>
 * Contains the set of points \((x,y)\) that fulfill the weierstrass equation.
 * In short form, the equation reduces to \(y^2 = x^3 + A4 \cdot x + A6\).
 */
public interface WeierstrassCurve extends EllipticCurve {


    /**
     * Returns \(A6\) from the weierstrass equation
     * \(y^2 + A1 \cdot xy + A3 \cdot y = x^3 + A2 \cdot x^2 + A4 \cdot x + A6\).
     */
    FieldElement getA6();

    /**
     * Returns \(A4\) from the weierstrass equation
     * \(y^2 + A1 \cdot xy + A3 \cdot y = x^3 + A2 \cdot x^2 + A4 \cdot x + A6\).
     */
    FieldElement getA4();

    /**
     * Returns \(A3\) from the weierstrass equation
     * \(y^2 + A1 \cdot xy + A3 \cdot y = x^3 + A2 \cdot x^2 + A4 \cdot x + A6\).
     * <p>
     * Is zero if the curve is given by a weierstrass equation in short form.
     */
    FieldElement getA3();

    /**
     * Returns \(A2\) from the weierstrass equation
     * \(y^2 + A1 \cdot xy + A3 \cdot y = x^3 + A2 \cdot x^2 + A4 \cdot x + A6\).
     * <p>
     * Is zero if the curve is given by a weierstrass equation in short form.
     */
    FieldElement getA2();

    /**
     * Returns \(A1\) from the weierstrass equation
     * \(y^2 + A1 \cdot xy + A3 \cdot y = x^3 + A2 \cdot x^2 + A4 \cdot x + A6\).
     * <p>
     * Is zero if the curve is given by a weierstrass equation in short form.
     */
    FieldElement getA1();

    /**
     * Construct an point on this curve given the x- and y-coordinates.
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the corresponding elliptic curve point
     */
    EllipticCurvePoint getElement(FieldElement x, FieldElement y);

    default boolean isShortForm() {
        return getA3().isZero() && getA2().isZero() && getA1().isZero();
    }
}
