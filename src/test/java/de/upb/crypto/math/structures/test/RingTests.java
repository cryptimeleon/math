package de.upb.crypto.math.structures.test;

import de.upb.crypto.math.interfaces.structures.*;
import de.upb.crypto.math.structures.integers.IntegerElement;
import de.upb.crypto.math.structures.integers.IntegerRing;
import de.upb.crypto.math.structures.polynomial.PolynomialRing;
import de.upb.crypto.math.structures.polynomial.PolynomialRing.Polynomial;
import de.upb.crypto.math.structures.polynomial.Seed;
import de.upb.crypto.math.structures.quotient.F2FiniteFieldExtension;
import de.upb.crypto.math.structures.quotient.F2FiniteFieldExtension.F2FiniteFieldElement;
import de.upb.crypto.math.structures.quotient.FiniteFieldExtension;
import de.upb.crypto.math.structures.quotient.FiniteFieldExtension.FiniteFieldElement;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Does generic testing of rings
 */
@RunWith(Parameterized.class)
public class RingTests extends StructureTests {
    private Ring ring;
    private Supplier<RingElement> elementSupplier;
    private Supplier<RingElement> unitElementSupplier;

    public RingTests(TestParams params) {
        this.ring = params.ring;
        this.elementSupplier = params.elementSupplier;
        this.unitElementSupplier = params.unitElementSupplier;
    }

    @Test
    public void testField() {
        if (!(ring instanceof Field))
            return;

        Field field = (Field) ring;

        RingElement a = unitElementSupplier.get();
        RingElement b = unitElementSupplier.get();

        // Commutativity of *
        assertTrue(a.mul(b).equals(b.mul(a)));

        // getPrimitiveElement
        try {
            RingElement primitive = field.getPrimitiveElement();
            assertTrue(ring.size() != null); // finite size if no exception was thrown

            // TODO test generating property
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }

    @Test
    public void testBasicProperties() {
        RingElement a = null, b = null, c;

        // Drawing random elements works or throws the right exception
        try {
            a = ring.getUniformlyRandomUnit();
            assertTrue(a.isUnit());
            b = ring.getUniformlyRandomElement();
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedOperationException);
        }
        if (a != null)
            assert (a.inv().mul(a).equals(ring.getOneElement()));

        a = unitElementSupplier.get();
        b = elementSupplier.get();
        c = elementSupplier.get();


        if (b instanceof Polynomial) {
            ((Polynomial) b).evaluate(((PolynomialRing) b.getStructure()).getBaseRing().getUniformlyRandomElement());
        }

        // a/a = 1
        assertTrue(a.inv().mul(a).equals(ring.getOneElement()));

        // a-a = 0
        assertTrue(a.add(a.neg()).equals(ring.getZeroElement()));
        assertTrue(a.sub(a).equals(ring.getZeroElement()));

        // a-b = a+(-b)
        assertEquals(a.add(b.neg()), a.sub(b));

        // Associativity of *
        assertTrue(a.mul(b).mul(c).equals(a.mul(b.mul(c))));

        // Associativity, distributivity, and commutativity of +
        assertTrue(a.add(b).add(c).equals(a.add(b.add(c))));
        assertTrue(a.add(b).mul(c).equals(a.mul(c).add(b.mul(c))));
        assertTrue(a.add(b).equals(b.add(a)));

        // b/a*a = b
        assertTrue(b.div(a).mul(a).equals(b));
        assertTrue(b.mul(a.inv()).mul(a).equals(b));

        // 1*b = b
        assertTrue(b.mul(ring.getOneElement()).equals(b));
        assertTrue(ring.getOneElement().mul(b).equals(b));

        // 0*b = 0
        assertTrue(b.mul(ring.getZeroElement()).equals(ring.getZeroElement()));

        // inverting 0 throws UnsupportedOperationException
        Exception thrown = null;
        try {
            assertFalse(ring.getZeroElement().isUnit());
            ring.getZeroElement().inv();
        } catch (Exception ex) {
            thrown = ex;
        }
        assertTrue(thrown != null && thrown instanceof UnsupportedOperationException);

        // Characteristic
        try {
            BigInteger characteristic = ring.getCharacteristic();

            if (characteristic.equals(BigInteger.ZERO))
                assertNull("Characteristic zero should imply infinite ring size", ring.size());
            else if (characteristic.compareTo(BigInteger.valueOf(50000)) < 0) { // don't really want to try huge characteristics
                // Check that characteristic*b = 0
                RingElement x = ring.getZeroElement();
                for (int i = 0; i < characteristic.intValue(); i++)
                    x = x.add(b);

                assertTrue(x.isZero());
            }

        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        //Homomorphic map from the integers
        assertTrue(ring.getElement(0).equals(ring.getZeroElement()));
        assertTrue(ring.getElement(1).equals(ring.getOneElement()));
        assertTrue(ring.getElement(-1).equals(ring.getOneElement().neg()));
        assertTrue(ring.getElement(5).add(ring.getElement(8)).equals(ring.getElement(13)));
        assertTrue(ring.getElement(5).mul(ring.getElement(8)).equals(ring.getElement(40)));

        // Size
        try {
            BigInteger size = ring.size();
            BigInteger unitGroupSize = ring.sizeUnitGroup();
            assertTrue(unitGroupSize != null || size == null); // infinite unit group implies infinite size
            if (size != null)
                assertTrue(size.compareTo(unitGroupSize) >= 0);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        // Euclidean algorithm
        try {
            RingElement[] result = ring.extendedEuclideanAlgorithm(b, c);
            result = ring.extendedEuclideanAlgorithm(b, c);
            assertTrue(b.mul(result[0]).add(c.mul(result[1])).equals(result[2]));
            ArrayList<RingElement> result2 = ring.extendedEuclideanAlgorithm(Arrays.asList(b, c));
            assertTrue(b.mul(result2.get(0)).add(c.mul(result2.get(1))).equals(result2.get(2)));
        } catch (UnsupportedOperationException e) {
            // That's okay
        }
    }

    @Test
    public void testDivideWithRemainder() {
        RingElement a = elementSupplier.get();
        RingElement b = elementSupplier.get();
        if (b.isZero())
            b = ring.getOneElement();
        RingElement[] result;
        try {
            result = a.divideWithRemainder(b);
            assertTrue(result[0].mul(b).add(result[1]).equals(a));
            assertTrue(result[1].isZero() || result[1].getRank().compareTo(b.getRank()) < 0);
        } catch (Exception e) {
            if (!(e instanceof UnsupportedOperationException)) {
                System.out.println("Test Divide remainder: Error (" + e + ") by class " + b.getClass());
                e.printStackTrace();
            }
            assertTrue(e instanceof UnsupportedOperationException);
            return;
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        RingElement a = elementSupplier.get();
        RingElement b = a.mul(ring.getOneElement()); // b = a ("duplicated")

        if (a == b)
            System.out.println("Warning: could not test hash code implementation for " + ring); // if a == b, the default "Object" hashCode implementation will simply work just like that

        assertTrue(a.equals(b) && b.equals(b));
        assertTrue("Equal elements should have the same hashCode", a.hashCode() == b.hashCode());
    }


    @Test
    public void testUniqueRepresentations() {
        Optional<Integer> maxLength = Optional.empty();
        try {
            maxLength = ring.getUniqueByteLength();
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
        if (maxLength.isPresent()) {
            RingElement a = elementSupplier.get();
            RingElement b = elementSupplier.get();
            try {
                assertTrue(a.getUniqueByteRepresentation().length == maxLength.get());
                assertTrue(b.getUniqueByteRepresentation().length == maxLength.get());
            } catch (Exception e) {
                assertTrue(e instanceof UnsupportedOperationException);
            }
        }

    }

    @Parameters(name = "Test: {0}") // add (name="Test: {0}") for jUnit 4.12+ to print ring's name to test
    public static Collection<TestParams[]> data() {
        // Some setup
        // Z
        IntegerRing integerRing = new IntegerRing();

        // Zp
        Zp z13 = new Zp(BigInteger.valueOf(13));

        // Zn
        Zn z4 = new Zn(BigInteger.valueOf(4));

        // Polynomial ring over z13
        PolynomialRing polyRing = new PolynomialRing(z13);

        // Quotient ring equivalent to z13
        QuotientRingZ13TestImpl quotientZ13 = new QuotientRingZ13TestImpl();

        // Finite extension field F4 as F2[X]/(x^2+x+1)
        Zp f2 = new Zp(BigInteger.valueOf(2));
        FiniteFieldExtension f4 = new FiniteFieldExtension(f2, PolynomialRing.getPoly(f2.valueOf(1), f2.valueOf(1), f2.valueOf(1)));

        FiniteFieldExtension f76 = new FiniteFieldExtension(PolynomialRing.getPoly(f2.valueOf(1), f2.valueOf(0), f2.valueOf(0), f2.valueOf(0), f2.valueOf(1), f2.valueOf(1)));
        FiniteFieldElement f77 = f76.new FiniteFieldElement(PolynomialRing.getPoly(f2.valueOf(1), f2.valueOf(1), f2.valueOf(1), f2.valueOf(1), f2.valueOf(1), f2.valueOf(1), f2.valueOf(1)));


        F2FiniteFieldExtension f90 = new F2FiniteFieldExtension(PolynomialRing.getPoly(f2.valueOf(1), f2.valueOf(0), f2.valueOf(0), f2.valueOf(0), f2.valueOf(1), f2.valueOf(1)));
        F2FiniteFieldElement f91 = f90.new F2FiniteFieldElement(PolynomialRing.getPoly(f2.valueOf(1), f2.valueOf(1), f2.valueOf(1), f2.valueOf(1), f2.valueOf(1), f2.valueOf(1), f2.valueOf(1)));

        PolynomialRing galois = new PolynomialRing(f2);
        Supplier<RingElement> galoisSupplier = new Supplier<RingElement>() {

            @Override
            public RingElement get() {
                byte[] coeff = new byte[8];
                new Random().nextBytes(coeff);

                return galois.new Polynomial(new Seed(coeff));
            }

        };

        Supplier<RingElement> f2Supp = new Supplier<RingElement>() {

            @Override
            public RingElement get() {
                byte[] coeff = new byte[8];
                new Random().nextBytes(coeff);

                return f90.new F2FiniteFieldElement(galois.new Polynomial(new Seed(coeff)));
            }
        };

        // Finite extension field F16 from F4 as F4[Y]/(y^2+y+x) (x being an element of F2[X]/(...) ~= F16) //alternative: y^2+xy+1
        FiniteFieldExtension f16 = new FiniteFieldExtension(f4,
                PolynomialRing.getPoly(f4.createElement(PolynomialRing.getPoly(f2.valueOf(0), f2.valueOf(1))), // x in F4[X]
                        f4.createElement(PolynomialRing.getPoly(f2.valueOf(1))), // 1 in F4[X]
                        f4.createElement(PolynomialRing.getPoly(f2.valueOf(1))) // 1 in F4[X]
                ) // y^2+y+x
        );

        // Finite extension field F_p/(x^2+1) where p = 3 mod 4 (such that x^2+1 is irreducible)
        Zp z17 = new Zp(BigInteger.valueOf(17));
        FiniteFieldExtension z17extension = new FiniteFieldExtension(z17, PolynomialRing.getPoly(z17.valueOf(1), z17.valueOf(0), z17.valueOf(1)));

        // Collect parameters
        TestParams params[][] = new TestParams[][]{{new TestParams(integerRing, () -> new IntegerElement(5), () -> new IntegerElement(-1))}, {new TestParams(z13)},
                {new TestParams(z4, () -> z4.createZnElement(BigInteger.valueOf(2)), () -> z4.createZnElement(BigInteger.valueOf(3)))},
                {new TestParams(polyRing, () -> polyRing.new Polynomial(new Random().nextBoolean() ? z13.getUniformlyRandomElement() : z13.getZeroElement(), z13.getUniformlyRandomElement()), polyRing::getUniformlyRandomUnit)},
                {new TestParams(quotientZ13)}, {new TestParams(f4)}, {new TestParams(f16)}, {new TestParams(z17extension)}, {new TestParams(f90, f2Supp, f90::getUniformlyRandomUnit)}, {new TestParams(galois, galoisSupplier, galois::getUniformlyRandomUnit)},};
        return Arrays.asList(params);
    }

    private static class TestParams {
        Ring ring;
        Supplier<RingElement> elementSupplier;
        Supplier<RingElement> unitElementSupplier;

        /**
         * Test parameters
         *
         * @param ring                the ring to test
         * @param elementSupplier     a supplier for (possibly random) elements to test with
         * @param unitElementSupplier like elementSupplier but must return units (i.e. invertible elements)
         */
        public TestParams(Ring ring, Supplier<RingElement> elementSupplier, Supplier<RingElement> unitElementSupplier) {
            this.ring = ring;
            this.elementSupplier = elementSupplier;
            this.unitElementSupplier = unitElementSupplier;
        }

        /**
         * Test parameters where the elements used for the test can just be drawn uniformly from the ring
         *
         * @param ring
         */
        public TestParams(Ring ring) {
            this(ring, ring::getUniformlyRandomElement, ring::getUniformlyRandomUnit);
        }

        @Override
        public String toString() {
            return ring.getClass().getName() + " - " + ring.toString();
        }
    }

    @Override
    public Structure getStructureToTest() {
        return ring;
    }

    @Override
    public Element getElementToTest() {
        return elementSupplier.get();
    }
}
