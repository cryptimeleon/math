package org.cryptimeleon.math.structures;

import org.cryptimeleon.math.structures.rings.Field;
import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionField;
import org.cryptimeleon.math.structures.rings.integers.IntegerElement;
import org.cryptimeleon.math.structures.rings.integers.IntegerRing;
import org.cryptimeleon.math.structures.rings.polynomial.PolynomialRing;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zp;
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
    protected Ring ring;
    protected Supplier<RingElement> elementSupplier;
    protected Supplier<RingElement> unitElementSupplier;

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
        assertEquals(a.mul(b), b.mul(a));

        // getPrimitiveElement
        try {
            RingElement primitive = field.getUnitGroupGenerator();
            assertNotNull(ring.size()); // finite size if no exception was thrown

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
        assert a == null || (a.inv().mul(a).equals(ring.getOneElement()));

        a = unitElementSupplier.get();
        b = elementSupplier.get();
        c = elementSupplier.get();


        if (b instanceof PolynomialRing.Polynomial) {
            ((PolynomialRing.Polynomial) b).evaluate(((PolynomialRing) b.getStructure()).getBaseRing().getUniformlyRandomElement());
        }

        // 2*a = a+a
        assertEquals(a.mul(BigInteger.valueOf(2)), a.add(a));

        // a/a = 1
        assertEquals(a.inv().mul(a), ring.getOneElement());

        // a-a = 0
        assertEquals(a.add(a.neg()), ring.getZeroElement());
        assertEquals(a.sub(a), ring.getZeroElement());

        // a-b = a+(-b)
        assertEquals(a.add(b.neg()), a.sub(b));

        // Associativity of *
        assertEquals(a.mul(b).mul(c), a.mul(b.mul(c)));

        // Associativity, distributivity, and commutativity of +
        assertEquals(a.add(b).add(c), a.add(b.add(c)));
        assertEquals(a.add(b).mul(c), a.mul(c).add(b.mul(c)));
        assertEquals(a.add(b), b.add(a));

        // b/a*a = b
        assertEquals(b.div(a).mul(a), b);
        assertEquals(b.mul(a.inv()).mul(a), b);

        // 1*b = b
        assertEquals(b.mul(ring.getOneElement()), b);
        assertEquals(ring.getOneElement().mul(b), b);

        // 0*b = 0
        assertEquals(b.mul(ring.getZeroElement()), ring.getZeroElement());

        // inverting 0 throws UnsupportedOperationException
        Exception thrown = null;
        try {
            assertFalse(ring.getZeroElement().isUnit());
            ring.getZeroElement().inv();
        } catch (Exception ex) {
            thrown = ex;
        }
        assertTrue(thrown instanceof UnsupportedOperationException);

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
        assertEquals(ring.getElement(0), ring.getZeroElement());
        assertEquals(ring.getElement(1), ring.getOneElement());
        assertEquals(ring.getElement(-1), ring.getOneElement().neg());
        assertEquals(ring.getElement(5).add(ring.getElement(8)), ring.getElement(13));
        assertEquals(ring.getElement(5).mul(ring.getElement(8)), ring.getElement(40));

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
            assertEquals(b.mul(result[0]).add(c.mul(result[1])), result[2]);
            ArrayList<RingElement> result2 = ring.extendedEuclideanAlgorithm(Arrays.asList(b, c));
            assertEquals(b.mul(result2.get(0)).add(c.mul(result2.get(1))), result2.get(2));
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
            assertEquals(result[0].mul(b).add(result[1]), a);
            assertTrue(result[1].isZero() || result[1].getRank().compareTo(b.getRank()) < 0);
        } catch (Exception e) {
            if (!(e instanceof UnsupportedOperationException)) {
                System.out.println("Test Divide remainder: Error (" + e + ") by class " + b.getClass());
                e.printStackTrace();
            }
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        RingElement a = elementSupplier.get();
        RingElement b = a.mul(ring.getOneElement()); // b = a ("duplicated")

        if (a == b)
            System.out.println("Warning: could not test hash code implementation for " + ring); // if a == b, the default "Object" hashCode implementation will simply work just like that

        assertTrue(a.equals(b) && b.equals(b));
        assertEquals("Equal elements should have the same hashCode", a.hashCode(), b.hashCode());
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
                assertEquals(a.getUniqueByteRepresentation().length, (int) maxLength.get());
                assertEquals(b.getUniqueByteRepresentation().length, (int) maxLength.get());
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

        // ExtensionField
        ExtensionField extensionField = new ExtensionField(z13.getElement(11), 2);

        // Collect parameters
        TestParams[][] params = new TestParams[][]{
                {new TestParams(integerRing, () -> new IntegerElement(5), () -> new IntegerElement(-1))},
                {new TestParams(z13)},
                {new TestParams(z4, () -> z4.createZnElement(BigInteger.valueOf(2)),
                        () -> z4.createZnElement(BigInteger.valueOf(3)))},
                {new TestParams(polyRing,
                        () -> polyRing.new Polynomial(new Random().nextBoolean() ? z13.getUniformlyRandomElement() :
                                z13.getZeroElement(),
                                z13.getUniformlyRandomElement()),
                        polyRing::getUniformlyRandomUnit)},
                {new TestParams(extensionField)}
        };
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
