package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.serialization.standalone.StandaloneReprSubTest;
import org.cryptimeleon.math.structures.groups.basic.BasicBilinearGroup;
import org.cryptimeleon.math.structures.groups.basic.BasicGroup;
import org.cryptimeleon.math.structures.groups.cartesian.ProductGroup;
import org.cryptimeleon.math.structures.groups.debug.DebugBilinearGroup;
import org.cryptimeleon.math.structures.groups.debug.DebugBilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.debug.DebugGroupImplNoExpMultiExp;
import org.cryptimeleon.math.structures.groups.debug.DebugGroupImplTotal;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular.SupersingularBasicBilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular.SupersingularBilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.type3.bn.BarretoNaehrigBasicBilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.type3.bn.BarretoNaehrigBilinearGroup;
import org.cryptimeleon.math.structures.groups.lazy.LazyBilinearGroup;
import org.cryptimeleon.math.structures.groups.sn.Sn;
import org.cryptimeleon.math.structures.rings.cartesian.ProductRing;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionField;
import org.cryptimeleon.math.structures.rings.polynomial.PolynomialRing;
import org.cryptimeleon.math.structures.rings.zn.*;

import java.math.BigInteger;

public class StructureStandaloneReprTest extends StandaloneReprSubTest {
    private final Zn zn = new Zn(BigInteger.valueOf(100000));
    private final Zp zp = new Zp(BigInteger.valueOf(100003));

    public void testBilinearGroup(BilinearGroup bilGroup) {
        test(bilGroup);
        test(bilGroup.getG1());
        test(bilGroup.getG2());
        test(bilGroup.getGT());
        try {
            test(bilGroup.getHashIntoG1());
        } catch (UnsupportedOperationException ignored) {}
        try {
            test(bilGroup.getHashIntoG2());
        } catch (UnsupportedOperationException ignored) {}
        try {
            test(bilGroup.getHashIntoGT());
        } catch (UnsupportedOperationException ignored) {}
        try {
            test(bilGroup.getHomomorphismG2toG1());
        } catch (UnsupportedOperationException ignored) {}
    }

    public void testBilinearGroupImpl(BilinearGroupImpl bilGroup) {
        test(bilGroup);
        test(bilGroup.getG1());
        test(bilGroup.getG2());
        test(bilGroup.getGT());
        try {
            test(bilGroup.getHashIntoG1());
        } catch (UnsupportedOperationException ignored) {}
        try {
            test(bilGroup.getHashIntoG2());
        } catch (UnsupportedOperationException ignored) {}
        try {
            test(bilGroup.getHashIntoGT());
        } catch (UnsupportedOperationException ignored) {}
        try {
            test(bilGroup.getHomomorphismG2toG1());
        } catch (UnsupportedOperationException ignored) {}
    }

    public void testBarretoNaehrig() {
        testBilinearGroup(new BarretoNaehrigBasicBilinearGroup(80));
        testBilinearGroup(new BarretoNaehrigBilinearGroup(80));
    }

    public void testSupersingular() {
        testBilinearGroup(new SupersingularBasicBilinearGroup(80));
        testBilinearGroup(new SupersingularBilinearGroup(80));
    }

    public void testLazyAndBasicGroup() {
        BilinearGroupImpl bilGroupImpl = new DebugBilinearGroupImpl(RandomGenerator.getRandomPrime(128),
                BilinearGroup.Type.TYPE_3, false);

        testBilinearGroup(new LazyBilinearGroup(bilGroupImpl));
        testBilinearGroup(new BasicBilinearGroup(bilGroupImpl));
    }

    public void testDebugGroup() {
        testBilinearGroup(new DebugBilinearGroup(RandomGenerator.getRandomPrime(128),
                BilinearGroup.Type.TYPE_1));
        testBilinearGroupImpl(new DebugBilinearGroupImpl(RandomGenerator.getRandomPrime(128),
                BilinearGroup.Type.TYPE_1, false));
        testBilinearGroupImpl(new DebugBilinearGroupImpl(RandomGenerator.getRandomPrime(128),
                BilinearGroup.Type.TYPE_1, true));
        test(new BasicGroup(new DebugGroupImplTotal("DGIT", RandomGenerator.getRandomPrime(128))));
        test(new BasicGroup(new DebugGroupImplNoExpMultiExp("DGINEME", RandomGenerator.getRandomPrime(128))));
    }

    public void testExtensionField() {
        test(new ExtensionField(BigInteger.valueOf(17)));
    }

    public void testProductStructures() {
        test(new ProductGroup(
                zn.asAdditiveGroup(),
                zp.asUnitGroup()
                )
        );
        test(new ProductRing(
                zp,
                zn
                )
        );
    }

    public void testRingGroups() {
        test(zp.asUnitGroup());
        test(zp.asAdditiveGroup());
    }

    public void testRings() {
        test(zp);
        test(zn);

        test(new HashIntoZn(zp));
        test(new HashIntoZp(zp));
        test(new HashIntoZnAdditiveGroup(zp));

        test(new PolynomialRing(zp));
    }

    public void testSn() {
        test(new Sn(20));
    }
}
