package de.upb.crypto.math.structures.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.structures.*;
import de.upb.crypto.math.lazy.LazyGroup;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigBilinearGroup;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigProvider;
import de.upb.crypto.math.pairings.debug.DebugGroup;
import de.upb.crypto.math.pairings.supersingular.SupersingularProvider;
import de.upb.crypto.math.pairings.supersingular.SupersingularTateGroup;
import de.upb.crypto.math.random.interfaces.RandomUtil;
import de.upb.crypto.math.structures.integers.IntegerRing;
import de.upb.crypto.math.structures.sn.Sn;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.MyAffineCurve;
import de.upb.crypto.math.swante.MyJacobiCurve;
import de.upb.crypto.math.swante.MyProjectiveCurve;
import de.upb.crypto.math.swante.util.MyShortFormWeierstrassCurveParameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigInteger;
import java.util.ArrayList;
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
public class GroupTests extends StructureTests {
    private Group group;
    private Supplier<GroupElement> elementSupplier;

    public GroupTests(TestParams params) {
        this.group = params.group;
        this.elementSupplier = params.elementSupplier;
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
        long exponent = 10;
        if (group.isCommutative())
            assertEquals("Exponentiation+Commutativity", a.op(b).pow(exponent), a.pow(exponent).op(b.pow(exponent)));

        // Neutral element
        assertEquals(a.op(group.getNeutralElement()), a);
        assertEquals(group.getNeutralElement().op(a), a);

        //Exponentiation
        GroupElement aToTheFifth = a.op(a).op(a).op(a).op(a);
        assertEquals("Exponentiation", a.pow(BigInteger.valueOf(5)), aToTheFifth);
        assertEquals("Exponentiation with negative exponent", a.pow(BigInteger.valueOf(-5)), aToTheFifth.inv());

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
            assertTrue("Lagrange inversion", a.pow(size.subtract(BigInteger.ONE)).equals(a.inv()));

            Zn.ZnElement r = new Zn(group.size()).getUniformlyRandomElement();
            if (size.isProbablePrime(100)) {
                // If commutative: (ab)^r b^{-r} = a^r
                assertTrue(a.op(b).pow(r).op(b.pow(r.neg())).equals(a.pow(r)));
            } else {
                // Otherwise (or if not sure): a^r b^r b^{-r} = a^r
                assertTrue(a.pow(r).op(b.pow(r)).op(b.pow(r.neg())).equals(a.pow(r)));
            }
        }
    }

    @Test
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
    }

    @Test
    public void testEqualsAndHashCode() {
        GroupElement a = elementSupplier.get();
        GroupElement b = a.op(a).op(a.inv()); // b = a ("duplicated")

        if (a == b)
            System.out.println("Warning: could not test hash code implementation for " + group); // if a == b, the default "Object" hashCode implementation will simply work just like that

        assertTrue(a.equals(b) && b.equals(b)); // todo: b.equals(a)??
        assertTrue("Equal elements should have the same hashCode", a.hashCode() == b.hashCode());
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

    @Parameters(name = "Test: {0}") // add (name="Test: {0}") for jUnit 4.12+ to print group's name to test
    public static Collection<TestParams> data() {
        // Some setup
        // Unit group of a ring
        RingUnitGroup ringUnitGroup = new RingUnitGroup(new Zp(BigInteger.valueOf(13)));

        // Additive group of a ring
        RingAdditiveGroup ringAddGroup = new RingAdditiveGroup(new Zn(BigInteger.valueOf(12)));
        RingAdditiveGroup ringAddGroupInt = new RingAdditiveGroup(new IntegerRing());

        // Debug group
        DebugGroup debugGroup = new DebugGroup("Testgroup", BigInteger.valueOf(10));

        // Sn
        Sn sn = new Sn(10);

        // Supersingular curve groups
		/*SupersingularProvider supsingfac = new SupersingularProvider();
		supsingfac.init(80);
		Group supsingG1 = supsingfac.getG1();
		Group supsingGT = supsingfac.getGT();*/

        // Supersingular curve groups
        SupersingularProvider supsingFac = new SupersingularProvider();
        SupersingularTateGroup supsingGrp = supsingFac.provideBilinearGroup(80, new BilinearGroupRequirement(BilinearGroup.Type.TYPE_1, true, true, false));
        Group supsingG1 = supsingGrp.getG1();
        Group supsingGT = supsingGrp.getGT();

        //BarretoNaehrig groups
        BarretoNaehrigProvider bnFac = new BarretoNaehrigProvider();
        BarretoNaehrigBilinearGroup bnGrp = bnFac.provideBilinearGroup(256, new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3, true, true, false));
        Group bnG1 = bnGrp.getG1();
        Group bnG2 = bnGrp.getG2();
        Group bnGT = bnGrp.getGT();

        //Lazy group
        LazyGroup lazyGroup = new LazyGroup(bnGT);

        // Collect parameters
//        TestParams params[][] = new TestParams[][]{
//                {new TestParams(ringUnitGroup)}, {new TestParams(ringAddGroup)},
//                {new TestParams(ringAddGroupInt, () -> ringAddGroupInt.new RingAdditiveGroupElement(new IntegerElement(RandomGeneratorSupplier.getRnd().getRandomElement(BigInteger.valueOf(100000)))))}, {new TestParams(sn)},
//                {new TestParams(debugGroup)},
//                {new TestParams(supsingG1)}, {new TestParams(supsingGT)},
//                {new TestParams(bnG1)}, {new TestParams(bnG2)}, {new TestParams(bnGT)},
//                {new TestParams(lazyGroup)}
//        };
        MyShortFormWeierstrassCurveParameters mySecp256r1Parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
        TestParams params[] = new TestParams[]{
                new TestParams(new MyAffineCurve(mySecp256r1Parameters)),
                new TestParams(new MyProjectiveCurve(mySecp256r1Parameters)),
                new TestParams(new MyJacobiCurve(mySecp256r1Parameters))
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

    @Override
    public Structure getStructureToTest() {
        return group;
    }

    @Override
    public Element getElementToTest() {
        return elementSupplier.get();
    }
}
