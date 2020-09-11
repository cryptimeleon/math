package de.upb.crypto.math.structures.test;

import de.upb.crypto.math.elliptic.Secp256k1;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupImpl;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.RingAdditiveGroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.RingUnitGroupImpl;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigProvider;
import de.upb.crypto.math.pairings.debug.DebugGroupImpl;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.serialization.RepresentableRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.integers.IntegerElement;
import de.upb.crypto.math.structures.integers.IntegerRing;
import de.upb.crypto.math.structures.sn.Sn;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Does generic testing of groups
 */
@RunWith(Parameterized.class)
public class GroupImplTests {
    protected GroupImpl groupImpl;
    protected Supplier<GroupElementImpl> elementSupplier;

    public GroupImplTests(TestParams params) {
        this.groupImpl = params.group;
        this.elementSupplier = params.elementSupplier;
    }

    @Test
    public void testBasicProperties() {
        GroupElementImpl a = null, b = null, c;

        // Drawing random elements works or throws the right exception
        try {
            a = groupImpl.getUniformlyRandomElement();
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedOperationException);
        }

        // Drawing uniformly random non neutral elements works or throws the right exception
        try {
            a = groupImpl.getUniformlyRandomNonNeutral();
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedOperationException);
        }

        a = elementSupplier.get();
        b = elementSupplier.get();
        c = elementSupplier.get();

        // a/a = 1
        assertTrue(a.inv().op(a).equals(groupImpl.getNeutralElement()));
        assertTrue(a.op(a.inv()).equals(groupImpl.getNeutralElement()));

        // Associativity
        assertTrue(a.op(b).op(c).equals(a.op(b.op(c))));

        // Commutativity
        if (groupImpl.isCommutative())
            assertEquals("Commutativity", a.op(b), b.op(a));

        //(ab)^x = a^xb^x
        BigInteger exponent = BigInteger.TEN;
        if (groupImpl.isCommutative())
            assertEquals("Exponentiation+Commutativity", a.op(b).pow(exponent), a.pow(exponent).op(b.pow(exponent)));

        // Neutral element
        assertTrue(a.op(groupImpl.getNeutralElement()).equals(a));
        assertTrue(groupImpl.getNeutralElement().op(a).equals(a));

        //Exponentiation
        GroupElementImpl aToTheFifth = a.op(a).op(a).op(a).op(a);
        assertEquals("Exponentiation", a.pow(BigInteger.valueOf(5)), aToTheFifth);
        assertEquals("Exponentiation with negative exponent", a.pow(BigInteger.valueOf(-5)), aToTheFifth.inv());

        // Size
        BigInteger size = null;
        try {
            size = groupImpl.size();
            assertTrue(size == null || size.signum() >= 0);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        if (size != null) {
            // Lagrange
            assertTrue("Lagrange", a.pow(size).isNeutralElement());
            assertTrue("Lagrange inversion", a.pow(size.subtract(BigInteger.ONE)).equals(a.inv()));

            BigInteger r = new Zn(groupImpl.size()).getUniformlyRandomElement().getInteger();
            if (size.isProbablePrime(100)) {
                // If commutative: (ab)^r b^{-r} = a^r
                assertTrue(a.op(b).pow(r).op(b.pow(r.negate())).equals(a.pow(r)));
            } else {
                // Otherwise (or if not sure): a^r b^r b^{-r} = a^r
                assertTrue(a.pow(r).op(b.pow(r)).op(b.pow(r.negate())).equals(a.pow(r)));
            }
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
        GroupElementImpl a = elementSupplier.get();
        GroupElementImpl b = a.op(a).op(a.inv()); // b = a ("duplicated")

        if (a == b)
            System.out.println("Warning: could not test hash code implementation for " + groupImpl); // if a == b, the default "Object" hashCode implementation will simply work just like that

        assertTrue(a.equals(b) && b.equals(a));
        assertTrue("Equal elements should have the same hashCode", a.hashCode() == b.hashCode());
    }

    @Test
    public void testUniqueRepresentations() {
        Optional<Integer> ubrLength = Optional.empty();
        try {
            ubrLength = groupImpl.getUniqueByteLength();
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
        if (ubrLength.isPresent()) {
            GroupElementImpl a = elementSupplier.get();
            GroupElementImpl b = elementSupplier.get();
            try {
                assertEquals("ubr length", (long) ubrLength.get(), a.getUniqueByteRepresentation().length);
                assertEquals("ubr length", (long) ubrLength.get(), b.getUniqueByteRepresentation().length);
                assertTrue("Uniqueness", a.equals(b) || !Arrays.equals(a.getUniqueByteRepresentation(), b.getUniqueByteRepresentation()));
            } catch (Exception e) {
                assertTrue(e instanceof UnsupportedOperationException);
            }
        }

    }

    @Parameters(name = "Test: {0}") // add (name="Test: {0}") for jUnit 4.12+ to print group's name to test
    public static Collection<TestParams[]> data() {
        // Some setup
        // Unit group of a ring
        RingUnitGroupImpl ringUnitGroupImpl = new RingUnitGroupImpl(new Zp(BigInteger.valueOf(13)));

        // Additive group of a ring
        RingAdditiveGroupImpl ringAddGroup = new RingAdditiveGroupImpl(new Zn(BigInteger.valueOf(12)));
        RingAdditiveGroupImpl ringAddGroupInt = new RingAdditiveGroupImpl(new IntegerRing());

        // Debug group
        DebugGroupImpl debugGroupImpl = new DebugGroupImpl("Testgroup", BigInteger.valueOf(1000));

        // BarretoNaehrig
        BilinearGroupImpl bnGroup = new BarretoNaehrigProvider().provideBilinearGroupImpl(128, new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3));
        GroupImpl bnG1 = bnGroup.getG1(), bnG2 = bnGroup.getG2(), bnGT = bnGroup.getGT();

        BilinearGroupImpl bnGroup2 = new BarretoNaehrigProvider().provideBilinearGroupImpl(128, new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3));
        GroupImpl bnG12 = bnGroup2.getG1(), bnG22 = bnGroup2.getG2(), bnGT2 = bnGroup2.getGT();

        // Sn
        Sn sn = new Sn(10);

        // Collect parameters
        TestParams params[][] = new TestParams[][]{
                {new TestParams(ringUnitGroupImpl)}, {new TestParams(ringAddGroup)},
                {new TestParams(ringAddGroupInt, () -> ringAddGroupInt.new RingAdditiveGroupElementImpl(new IntegerElement(RandomGeneratorSupplier.getRnd().getRandomElement(BigInteger.valueOf(100000)))))}, {new TestParams(sn)},
                {new TestParams(debugGroupImpl)},
                {new TestParams(bnG1)}, {new TestParams(bnG2)}, {new TestParams(bnGT)},
                {new TestParams(bnG12)}, {new TestParams(bnG22)}, {new TestParams(bnGT2)},
                {new TestParams(new Secp256k1())}
        };
        return Arrays.asList(params);
    }

    protected static class TestParams {
        GroupImpl group;
        Supplier<GroupElementImpl> elementSupplier;

        /**
         * Test parameters
         *
         * @param elementSupplier a supplier for (possibly random) elements to test with
         */
        public TestParams(GroupImpl group, Supplier<GroupElementImpl> elementSupplier) {
            this.group = group;
            this.elementSupplier = elementSupplier;
        }

        /**
         * Test parameters where the elements used for the test can just be drawn uniformly from the group
         */
        public TestParams(GroupImpl group) {
            this(group, group::getUniformlyRandomElement);
        }

        @Override
        public String toString() {
            return group.getClass().getName() + " - " + group.toString();
        }
    }

    @Test
    public void testStructureRepresentation() {
        RepresentableRepresentation repr = new RepresentableRepresentation(groupImpl);
        GroupImpl s2 = (GroupImpl) repr.recreateRepresentable();
        assertEquals("Reserialized GroupImpl should be equal to original", groupImpl, s2);
        assertEquals("Reserialized GroupImpl's hashCode should be equal to original", groupImpl.hashCode(), s2.hashCode());
    }

    @Test
    public void testElementRepresentation() {
        GroupElementImpl elem = elementSupplier.get();

        Representation repr = elem.getRepresentation();
        GroupElementImpl elem2 = groupImpl.getElement(repr);

        assertEquals("Reserialized element should be equal to original", elem, elem2);
        assertEquals("Reserialized element's hashCode should be equal to original", elem.hashCode(), elem2.hashCode());
    }
}
