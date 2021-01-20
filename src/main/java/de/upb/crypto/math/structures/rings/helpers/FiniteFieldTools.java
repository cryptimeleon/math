package de.upb.crypto.math.structures.rings.helpers;

import de.upb.crypto.math.structures.rings.Field;
import de.upb.crypto.math.structures.rings.FieldElement;

import java.math.BigInteger;

/**
 * Contains miscellaneous methods for working with finite fields.
 */
public abstract class FiniteFieldTools {

    /**
     * Returns true iff there is an \(y\) in the same field such that \(y^2 = x\).
     */
    public static boolean isSquare(FieldElement x) {
        if (x.isZero())
            return true;

        if (x.getStructure().getCharacteristic().equals(BigInteger.valueOf(2)))
            throw new UnsupportedOperationException();

        //for cyclic groups we can determine the Legendre symbol
        return x.pow(x.getStructure().sizeUnitGroup().divide(new BigInteger("2"))).isOne();
        /*
         * Proof: Let q be the unit group size. q is even. Let g be a generator of the unit group, g^z = x.
         * If x is a quadratic residue and y^2 = x, then x^(q/2) = (y^2)^(q/2) = 1.
         * If x^(q/2) = 1, then g^(zq/2) = 1, i.e. zq/2 is a multiple of q (over the integers).
         * Hence z must be even and so g^(z/2) is a square root of x.
         */
    }

    /**
     * Computes the square root of the given element.
     */
    public static FieldElement sqrt(FieldElement element) {
        if (!isSquare(element)) {
            throw new ArithmeticException(element + "is not a square");
        }

        if (element.isZero())
            return element;

        /* order of multiplicative group of the field */
        Field field = element.getStructure();
        BigInteger q = field.sizeUnitGroup();
        FieldElement t, r;

        /* implementation of Tonelli-Shanks */

        FieldElement z = field.getOneElement();

        int m;

        /*
         * search non-deterministically for QNR
         */
        do {
            z = field.getUniformlyRandomElement();
        } while (isSquare(z) || z.isZero());

        int s = 0;

        while (!q.testBit(0)) {
            q = q.shiftRight(1);
            s++;
        }

        z = z.pow(q);

        r = element.pow((q.add(BigInteger.valueOf(1)).divide(BigInteger.valueOf(2))));

        t = element.pow(q);

        m = s;

        while (!t.isOne()) {
            int i = 0;
            FieldElement t_squared = t;
            while (!t_squared.isOne()) {
                i++;
                t_squared = t_squared.mul(t_squared);
            }
            FieldElement b = z;
            for (int j = 0; j < m - i - 1; j++) {
                b = b.mul(b);
            }
            r = r.mul(b);
            b = b.mul(b);
            t = t.mul(b);
            z = b;
            m = i;
        }

        return r;
    }
}
