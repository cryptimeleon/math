package org.cryptimeleon.math.structures.groups.elliptic;

import org.cryptimeleon.math.structures.rings.FieldElement;

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

    /**
     * Checks whether the given point is on this curve.
     * 
     * @param x the x-coordinate of the point to check
     * @param y the y-coordinate of the point to check
     * @return true if the point is on this curve, false otherwise
     */
    default boolean isOnCurve(FieldElement x, FieldElement y) {
        // FieldElement x,y;
        //
        // x = p.getX();
        // y = p.getY();

        /*
         * check y^2+a_1 xy + a_3 y = x^3+a_2 x^2 + a_4 x + a_6
         *
         * rewritten as
         *
         * ((a_1 x + a_3)y + y)y = x ( x ( x+a_2 )+a_4)+a_6
         */
        return x.mul(getA1()).add(getA3()).mul(y).add(y).mul(y)
                .equals(x.add(getA2()).mul(x).add(getA4()).mul(x).add(getA6()));
    }

    default boolean isShortForm() {
        return getA3().isZero() && getA2().isZero() && getA1().isZero();
    }
}
