package org.cryptimeleon.math.structures;

import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupImpl;
import org.cryptimeleon.math.structures.groups.basic.BasicGroup;
import org.cryptimeleon.math.structures.groups.debug.DebugGroupImpl;
import org.cryptimeleon.math.structures.groups.lazy.LazyGroup;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Does generic testing of groups
 */
@RunWith(Parameterized.class)
public class GroupTests {
    protected Group group;
    protected Supplier<GroupElement> elementSupplier;

    public GroupTests(TestParams params) {
        this.group = params.group;
        this.elementSupplier = params.elementSupplier;
    }

    @Test
    public void testMultiexp() {
        try {
            group.size();
        } catch (UnsupportedOperationException e) {
            return; //don't test unknown order groups here, for simplicity.
        }
        int n = 20;

        GroupElement g = elementSupplier.get();
        List<GroupElement> h = Stream.generate(elementSupplier).limit(n).collect(Collectors.toList());
        List<Zn.ZnElement> exponents = Stream.generate(group::getUniformlyRandomExponent).limit(n).collect(Collectors.toList());

        //Computing g * h_i^x_i
        GroupElement resultMultiexp = g;
        for (int i=0; i<n; i++)
            resultMultiexp = resultMultiexp.op(h.get(i).pow(exponents.get(i)));

        GroupElement resultMultexpWay2 = g;
        for (int i=0; i<n; i++)
            resultMultexpWay2 = resultMultexpWay2.op(h.get(i).pow(exponents.get(i))).computeSync();

        resultMultiexp.compute();
        assertEquals(resultMultiexp, resultMultexpWay2);

        //Computing (g * h_i^x_i)^z
        Zn.ZnElement z = group.getUniformlyRandomUnitExponent();
        GroupElement everythingRaisedToZ = g.pow(z);
        for (int i=0; i<n; i++)
            everythingRaisedToZ = everythingRaisedToZ.op(h.get(i).pow(exponents.get(i).mul(z)));

        assertEquals(resultMultiexp.pow(z), everythingRaisedToZ);

        //Checking some exponent math
        List<Zn.ZnElement> otherExponents = Stream.generate(group::getUniformlyRandomExponent).limit(n).collect(Collectors.toList());
        Zn.ZnElement innerProduct = IntStream.range(0, n).mapToObj(i -> otherExponents.get(i).mul(exponents.get(i))).reduce(Zn.ZnElement::add).get();
        assertEquals(g.pow(innerProduct), IntStream.range(0, n).mapToObj(i -> g.pow(otherExponents.get(i)).pow(exponents.get(i))).reduce(GroupElement::op).get());
        assertEquals(g.pow(exponents.get(0)).pow(exponents.get(1)), g.pow(exponents.get(1)).pow(exponents.get(0)));
    }

    @Test
    public void testBasicProperties() {
        GroupElement a = null, b = null, c;

        // Drawing random elements works or throws the right exception
        try {
            a = group.getUniformlyRandomElement();
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedOperationException);
        }

        // Drawing uniformly random non neutral elements works or throws the right exception
        try {
            a = group.getUniformlyRandomNonNeutral();
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedOperationException);
        }

        a = elementSupplier.get();
        b = elementSupplier.get();
        c = elementSupplier.get();

        // a/a = 1
        assertEquals(a.inv().op(a), group.getNeutralElement());
        assertEquals(a.op(a.inv()), group.getNeutralElement());

        // Associativity
        assertEquals(a.op(b).op(c), a.op(b.op(c)));

        // Commutativity
        if (group.isCommutative())
            assertEquals("Commutativity", a.op(b), b.op(a));

        //(ab)^x = a^xb^x
        BigInteger exponent = BigInteger.TEN;
        if (group.isCommutative())
            assertEquals("Exponentiation+Commutativity", a.op(b).pow(exponent), a.pow(exponent).op(b.pow(exponent)));

        // Neutral element
        assertEquals(a.op(group.getNeutralElement()), a);
        assertEquals(group.getNeutralElement().op(a), a);

        //Exponentiation
        GroupElement aToTheFifth = a.op(a).op(a).op(a).op(a);
        assertEquals("Exponentiation", a.pow(5), aToTheFifth);
        assertEquals("Exponentiation with negative exponent", a.pow(-5), aToTheFifth.inv());

        // Size
        BigInteger size = null;
        try {
            size = group.size();
            assertTrue(size == null || size.signum() >= 0);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        if (size != null) {
            // Lagrange
            assertTrue("Lagrange", a.pow(size).isNeutralElement());
            assertEquals("Lagrange inversion", a.pow(size.subtract(BigInteger.ONE)), a.inv());

            BigInteger r = new Zn(group.size()).getUniformlyRandomElement().getInteger();
            if (size.isProbablePrime(100)) {
                // If commutative: (ab)^r b^{-r} = a^r
                assertEquals(a.op(b).pow(r).op(b.pow(r.negate())), a.pow(r));
            } else {
                // Otherwise (or if not sure): a^r b^r b^{-r} = a^r
                assertEquals(a.pow(r).op(b.pow(r)).op(b.pow(r.negate())), a.pow(r));
            }

            //Computing in the exponent
            assertEquals(a.pow(r.multiply(BigInteger.valueOf(42))), a.pow(r).pow(42));
        }
    }

    /*@Test
    public void testBatchOp() {
        ArrayList<GroupElement> elems = new ArrayList<>();
        ArrayList<BigInteger> exponents = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            elems.add(elementSupplier.get());
            exponents.add(RandomUtil.getRandomElement(group.size() == null ? BigInteger.valueOf(100) : group.size()));
        }
        elems.add(group.getNeutralElement());
        exponents.add(BigInteger.ONE);
        elems.add(elementSupplier.get());
        exponents.add(BigInteger.ZERO);
        elems.add(elementSupplier.get());
        exponents.add(BigInteger.valueOf(-5));
        elems.add(elementSupplier.get());
        exponents.add(BigInteger.valueOf(5));

        GroupElement resultNaive = group.getNeutralElement();
        for (int i = 0; i < elems.size(); i++) {
            resultNaive = resultNaive.op(elems.get(i).pow(exponents.get(i)));
        }

        GroupElement resultBatch = group.evaluate(elems, exponents);
        assertEquals("Batch operations", resultNaive, resultBatch);
    }*/

    @Test
    public void testEqualsAndHashCode() {
        GroupElement a = elementSupplier.get();
        GroupElement b = a.op(a).op(a.inv()); // b = a ("duplicated")

        if (a == b)
            System.out.println("Warning: could not test hash code ementation for " + group); // if a == b, the default "Object" hashCode ementation will sy work just like that

        assertTrue(a.equals(b) && b.equals(a));
        assertEquals("Equal elements should have the same hashCode", a.hashCode(), b.hashCode());
    }

    @Test
    public void testUniqueRepresentations() {
        Optional<Integer> ubrLength = Optional.empty();
        try {
            ubrLength = group.getUniqueByteLength();
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
        if (ubrLength.isPresent()) {
            GroupElement a = elementSupplier.get();
            GroupElement b = elementSupplier.get();
            try {
                assertEquals("ubr length", (long) ubrLength.get(), a.getUniqueByteRepresentation().length);
                assertEquals("ubr length", (long) ubrLength.get(), b.getUniqueByteRepresentation().length);
                assertTrue("Uniqueness", a.equals(b) || !Arrays.equals(a.getUniqueByteRepresentation(), b.getUniqueByteRepresentation()));
            } catch (Exception e) {
                assertTrue(e instanceof UnsupportedOperationException);
            }
        }
    }

    @Parameters(name = "Test: {0}")
    public static Collection<TestParams[]> data() {
        // Some setup
        GroupImpl debugGroupImpl = new DebugGroupImpl(
                "testGroupImpl", BigInteger.probablePrime(128, new Random())
        );
        BasicGroup basicGroup = new BasicGroup(debugGroupImpl);
        LazyGroup lazyGroup = new LazyGroup(debugGroupImpl);

        // Collect parameters
        TestParams[][] params = new TestParams[][]{
                {new TestParams(basicGroup)}, {new TestParams(lazyGroup)}
        };
        return Arrays.asList(params);
    }

    protected static class TestParams {
        Group group;
        Supplier<GroupElement> elementSupplier;

        /**
         * Test parameters
         *
         * @param elementSupplier a supplier for (possibly random) elements to test with
         */
        public TestParams(Group group, Supplier<GroupElement> elementSupplier) {
            this.group = group;
            this.elementSupplier = elementSupplier;
        }

        /**
         * Test parameters where the elements used for the test can just be drawn uniformly from the group
         */
        public TestParams(Group group) {
            this(group, group::getUniformlyRandomElement);
        }

        @Override
        public String toString() {
            return group.getClass().getName() + " - " + group.toString();
        }
    }

    @Test
    public void testStructureRepresentation() {
        RepresentableRepresentation repr = new RepresentableRepresentation(group);
        Group s2 = (Group) repr.recreateRepresentable();
        assertEquals("Reserialized Group should be equal to original", group, s2);
        assertEquals("Reserialized Group's hashCode should be equal to original", group.hashCode(), s2.hashCode());
    }

    @Test
    public void testElementRepresentation() {
        GroupElement elem = elementSupplier.get();

        Representation repr = elem.getRepresentation();
        GroupElement elem2 = group.restoreElement(repr);

        assertEquals("Reserialized element should be equal to original", elem, elem2);
        assertEquals("Reserialized element's hashCode should be equal to original", elem.hashCode(), elem2.hashCode());
    }
}
