package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.structures.polynomial.PolynomialRing;
import de.upb.crypto.math.structures.polynomial.PolynomialRing.Polynomial;
import de.upb.crypto.math.structures.quotient.F2FiniteFieldExtension;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.structures.zn.Zp.ZpElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class F2FiniteFieldParams {


    public static StandaloneTestParams get() {
        Zp zp = new Zp(BigInteger.valueOf(2));

        ZpElement ONE = zp.createZnElement(BigInteger.ONE);

        ZpElement ZERO = zp.createZnElement(BigInteger.ZERO);

        PolynomialRing baseRing = new PolynomialRing(zp);
        List<RingElement> coefficients = new ArrayList<>(4432);

        for (int i = 0; i <= 4432; i++) {
            if (i == 4431 || i == 47) {
                coefficients.add(ONE);
            } else {
                coefficients.add(ZERO);
            }
        }
        Polynomial expected = baseRing.new Polynomial(coefficients);
        F2FiniteFieldExtension field = new F2FiniteFieldExtension(expected);
        return new StandaloneTestParams(field.getClass(), field);
    }
}
