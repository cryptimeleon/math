package de.upb.crypto.math.structures.test;

import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.polynomial.PolynomialRing;
import de.upb.crypto.math.structures.polynomial.PolynomialRing.Polynomial;
import de.upb.crypto.math.structures.polynomial.Seed;
import de.upb.crypto.math.structures.quotient.F2FiniteFieldExtension;
import de.upb.crypto.math.structures.quotient.F2FiniteFieldExtension.F2FiniteFieldElement;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class F2FiniteFieldExtensionTest {

    private static final PolynomialRing polyRing = new PolynomialRing(new Zp(BigInteger.valueOf(2)));
    private static final Zp baseRing = new Zp(BigInteger.valueOf(2));

    private static final RingElement one = baseRing.createZnElement(BigInteger.ONE);
    private static final RingElement zero = baseRing.createZnElement(BigInteger.ZERO);

    @Test
    public void testIntermediateRepresentation() {
        // little endian, so 117 will be the encapsulated BigInt
        Polynomial test1 = polyRing.new Polynomial(one, zero, one, zero, one, one, one);
        Assert.assertTrue(F2FiniteFieldExtension.getEfficientPolynomial(test1).equals(BigInteger.valueOf(117)));

        // testing the setup via a seed:
        Polynomial test11 = polyRing.new Polynomial(new Seed(BigInteger.valueOf(117)));
        Assert.assertTrue(F2FiniteFieldExtension.getEfficientPolynomial(test11).equals(BigInteger.valueOf(117)));
        Assert.assertTrue(test1.equals(test11));
        // testing with a multiplicative of 8 as the coefficient length, should be 84 in bigInt
        Polynomial test2 = polyRing.new Polynomial(zero, zero, one, zero, one, zero, one);
        Assert.assertTrue(F2FiniteFieldExtension.getEfficientPolynomial(test2).equals(BigInteger.valueOf(84)));

        F2FiniteFieldExtension f2 = new F2FiniteFieldExtension(test1);
        F2FiniteFieldElement ftest2 = f2.new F2FiniteFieldElement(test2);
        F2FiniteFieldElement ftest22 = f2.new F2FiniteFieldElement(BigInteger.valueOf(84));

        Assert.assertTrue(ftest2.equals(ftest22));

        F2FiniteFieldElement ftest3 = f2.new F2FiniteFieldElement(polyRing.new Polynomial(one, one, zero, one));
        F2FiniteFieldElement ftest4 = f2.new F2FiniteFieldElement(polyRing.new Polynomial(one, one, one, one, one, zero, one));

        F2FiniteFieldElement res = (F2FiniteFieldElement) ftest3.add(ftest2);

        F2FiniteFieldElement res2 = ftest2.add(ftest3);

        Assert.assertTrue(res.equals(ftest4));
        Assert.assertTrue(res.equals(res2));
        // Polynomial
    }

    @Test
    public void testRepresentation() {
        Polynomial test1 = polyRing.new Polynomial(one, zero, one, zero, one, one, one);

        Polynomial test2 = polyRing.new Polynomial(zero, zero, one, zero, one, zero, one);

        F2FiniteFieldExtension f2 = new F2FiniteFieldExtension(test1);
        F2FiniteFieldElement ftest2 = f2.new F2FiniteFieldElement(test2);
        Representation r = ftest2.getRepresentation();
        Assert.assertTrue(f2.getElement(r).equals(ftest2));

        Representation r2 = f2.getRepresentation();
        Assert.assertTrue(f2.equals(new F2FiniteFieldExtension(r2)));
    }
}
