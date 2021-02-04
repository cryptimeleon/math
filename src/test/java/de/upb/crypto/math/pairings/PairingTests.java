package de.upb.crypto.math.pairings;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.structures.groups.GroupElement;
import de.upb.crypto.math.structures.groups.counting.CountingBilinearGroup;
import de.upb.crypto.math.structures.groups.elliptic.BilinearGroup;
import de.upb.crypto.math.structures.groups.elliptic.BilinearMap;
import de.upb.crypto.math.structures.groups.elliptic.type1.supersingular.SupersingularTateGroupImpl;
import de.upb.crypto.math.structures.groups.elliptic.type3.bn.BarretoNaehrigBilinearGroupImpl;
import de.upb.crypto.math.structures.groups.basic.BasicBilinearGroup;
import de.upb.crypto.math.structures.rings.zn.Zn;
import de.upb.crypto.math.structures.rings.zn.Zp;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RunWith(Parameterized.class)
public class PairingTests {
    private final BilinearMap pairing;

    public PairingTests(BilinearMap pairing) {
        this.pairing = pairing;
    }

    @Test
    public void testBasicProperties() {
        GroupElement p1 = pairing.getG1().getUniformlyRandomElement(), r1 = pairing.getG1().getUniformlyRandomElement();
        GroupElement p2 = pairing.getG2().getUniformlyRandomElement(), r2 = pairing.getG2().getUniformlyRandomElement();
        Zn.ZnElement randomExp1 = pairing.getG1().getUniformlyRandomNonzeroExponent();
        Zn.ZnElement randomExp2 = pairing.getG1().getUniformlyRandomNonzeroExponent();

        GroupElement t1, t2, t3, t4;

        //Lagrange and basic group properties (duplicate from GroupTests)
        assertEquals(p1.op(r1).op(r1.inv()), p1);
        assertTrue(p1.pow(pairing.getG1().size()).isNeutralElement());
        assertTrue(p2.pow(pairing.getG2().size()).isNeutralElement());

        //test only applies for symmetric type 1 pairings.
        if (pairing.isSymmetric()) {

            t1 = pairing.apply(p1, p2);
            t2 = pairing.apply(p2, p1);

            assertEquals(t1, t2);
        }

        //Bilinearity in first argument
        // e(P1+R1,P2)e(-R1,P2)=e(P1,P2)
        assertEquals("Bilinearity in first argument", pairing.apply(p1, p2), pairing.apply(p1.op(r1), p2).op(pairing.apply(r1.inv(), p2)));

        //Bilinearity in the second argument
        // e(P1,P2)+e(P1,R2) = e(P1,P2+R2)
        t1 = pairing.apply(p1, p2);
        t2 = pairing.apply(p1, r2);
        t3 = pairing.apply(p1, p2.op(r2));
        t4 = t1.op(t2);
        assertEquals(t4, t3);
        assertTrue(t1.pow(pairing.getG1().size()).isNeutralElement());
        assertFalse(t1.isNeutralElement());

        //Bilinearity in second argument
        // e(R1,P2+R2)e(-R1,P2)e(-R1,R2)=1
        assertTrue("Bilinearity in second argument", pairing.apply(r1, p2.op(r2)).op(pairing.apply(r1.inv(), p2)).op(pairing.apply(r1.inv(), r2)).isNeutralElement());

        //Bilinearity with exponent
        assertEquals(pairing.apply(p1.pow(randomExp1), p2), pairing.apply(p1, p2.pow(randomExp1)));
        assertEquals(pairing.apply(p1.pow(randomExp1), p2), pairing.apply(p1, p2).pow(randomExp1));
        assertEquals(pairing.apply(p1.pow(randomExp1), p2.pow(randomExp2)), pairing.apply(p1, p2).pow(randomExp1.mul(randomExp2)));

        //Basic other properties
        assertTrue(pairing.apply(pairing.getG1().getNeutralElement(), p2).isNeutralElement());
        assertTrue(pairing.apply(p1, pairing.getG2().getNeutralElement()).isNeutralElement());

        //e(x1*P1,x2*P2) = e(P1,P2)^{x1*x2}
        Zn zn = new Zn(pairing.getG1().size());
        Zn.ZnElement x1 = zn.getUniformlyRandomElement(), x2 = zn.getUniformlyRandomElement();
        assertEquals(pairing.apply(p1.pow(x1), p2.pow(x2)), pairing.apply(p1, p2).pow(x1.mul(x2)));
    }

    @Parameters(name = "Test: {0}") // add (name="Test: {0}") for jUnit 4.12+ to print Pairing's name to test
    public static Collection<BilinearMap[]> data() {
        // Counting curves
        BilinearGroup countingGroup1 =
                new CountingBilinearGroup(128, BilinearGroup.Type.TYPE_1);
        BilinearGroup countingGroup2 =
                new CountingBilinearGroup(128, BilinearGroup.Type.TYPE_2);
        BilinearGroup countingGroup3 =
                new CountingBilinearGroup(128, BilinearGroup.Type.TYPE_3);

        // Supersingular curve groups
        BilinearGroup supsingGroup = new BasicBilinearGroup(new SupersingularTateGroupImpl(80));

        // BN curves
        BilinearGroup bnGroup = new BasicBilinearGroup(new BarretoNaehrigBilinearGroupImpl(80));

        // Collect parameters
        BilinearMap[][] params = new BilinearMap[][] {
                {countingGroup1.getBilinearMap()}, {countingGroup2.getBilinearMap()}, {countingGroup3.getBilinearMap()},
                {supsingGroup.getBilinearMap()},
                {bnGroup.getBilinearMap()}
        };
        return Arrays.asList(params);
    }
}
