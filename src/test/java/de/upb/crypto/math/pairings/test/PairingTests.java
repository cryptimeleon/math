package de.upb.crypto.math.pairings.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.lazy.LazyPairing;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigProvider;
import de.upb.crypto.math.pairings.debug.DebugBilinearMap;
import de.upb.crypto.math.pairings.supersingular.SupersingularProvider;
import de.upb.crypto.math.pairings.supersingular.SupersingularTateGroup;
import de.upb.crypto.math.structures.ec.AffineECPCoordinate;
import de.upb.crypto.math.structures.ec.ProjectiveECPCoordinate;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class PairingTests {
    private BilinearMap pairing;

    public PairingTests(BilinearMap pairing) {
        this.pairing = pairing;
    }

    @Test
    public void testBasicProperties() {
        GroupElement p1 = pairing.getG1().getUniformlyRandomElement(), r1 = pairing.getG1().getUniformlyRandomElement();
        GroupElement p2 = pairing.getG2().getUniformlyRandomElement(), r2 = pairing.getG2().getUniformlyRandomElement();

        GroupElement t1, t2, t3, t4;

        //Lagrange and basic group properties (duplicate from GroupTests)
        assertTrue(p1.op(r1).op(r1.inv()).equals(p1));
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

        //Bilinearity in second argument
        // e(R1,P2+R2)e(-R1,P2)e(-R1,R2)=1
        assertTrue("Bilinearity in second argument", pairing.apply(r1, p2.op(r2)).op(pairing.apply(r1.inv(), p2)).op(pairing.apply(r1.inv(), r2)).isNeutralElement());

        //Basic other properties
        assertTrue(pairing.apply(pairing.getG1().getNeutralElement(), p2).isNeutralElement());
        assertTrue(pairing.apply(p1, pairing.getG2().getNeutralElement()).isNeutralElement());

        //e(x1*P1,x2*P2) = e(P1,P2)^{x1*x2}
        Zn zn = new Zn(pairing.getG1().size());
        Zn.ZnElement x1 = zn.getUniformlyRandomElement(), x2 = zn.getUniformlyRandomElement();
        assertTrue(pairing.apply(p1.pow(x1), p2.pow(x2)).equals(pairing.apply(p1, p2).pow(x1.mul(x2))));
    }

    @Test
    public void testExpressions() {
        GroupElement g[] = new GroupElement[5];
        GroupElement h[] = new GroupElement[g.length];
        Zp.ZpElement exp[] = new Zp.ZpElement[g.length];
        Zp zp = new Zp(pairing.getG1().size());

        for (int i = 0; i < g.length; i++) {
            g[i] = pairing.getG1().getUniformlyRandomElement();
            h[i] = pairing.getG2().getUniformlyRandomElement();
            exp[i] = zp.getUniformlyRandomElement();
        }
        exp[0] = zp.getZeroElement();

        //Compute result using expression
        PairingProductExpression expr = pairing.pairingProductExpression();
        for (int i = 0; i < g.length; i++) {
            expr.op(g[i], h[i], exp[i]);
        }
        GroupElement resultExpr = expr.evaluate();

        //Compute result naively using pow() in G1
        GroupElement naive = pairing.getGT().getNeutralElement();
        for (int i = 0; i < g.length; i++) {
            naive = naive.op(pairing.apply(g[i].pow(exp[i]), h[i]));
        }
        assertEquals(naive, resultExpr);

        //Compute result naively using pow() in G2
        naive = pairing.getGT().getNeutralElement();
        for (int i = 0; i < g.length; i++) {
            naive = naive.op(pairing.apply(g[i], h[i].pow(exp[i])));
        }
        assertEquals(naive, resultExpr);

        //Compute result naively using pow() in GT
        naive = pairing.getGT().getNeutralElement();
        for (int i = 0; i < g.length; i++) {
            naive = naive.op(pairing.apply(g[i], h[i]).pow(exp[i]));
        }
        assertEquals(naive, resultExpr);

        //Try nested expressions: e(g[i] * g[i+1], h[i])^2
        expr = pairing.pairingProductExpression();
        for (int i = 0; i + 1 < g.length; i += 2) {
            expr.op(g[i].asPowProductExpression().op(g[i + 1]).pow(exp[i]), h[i].asPowProductExpression(), BigInteger.valueOf(2));
        }
        resultExpr = expr.evaluate();
        naive = pairing.getGT().getNeutralElement();
        for (int i = 0; i + 1 < g.length; i += 2) {
            naive = naive.op(pairing.apply(g[i].op(g[i + 1]), h[i], exp[i].mul(zp.valueOf(2))));
        }
        assertEquals(resultExpr, naive);
    }

    @Parameters(name = "Test: {0}") // add (name="Test: {0}") for jUnit 4.12+ to print Pairing's name to test
    public static Collection<BilinearMap[]> data() {
        //Debug curve
        DebugBilinearMap debugMap1 = new DebugBilinearMap(1, BigInteger.valueOf(19));
        DebugBilinearMap debugMap2 = new DebugBilinearMap(2, BigInteger.valueOf(19));
        DebugBilinearMap debugMap3 = new DebugBilinearMap(3, BigInteger.valueOf(19));

        // Supersingular curve groups
        SupersingularProvider supsingFac = new SupersingularProvider();
        SupersingularTateGroup supSingGroupAffine = supsingFac.provideBilinearGroup(80,
                new BilinearGroupRequirement(BilinearGroup.Type.TYPE_1, true, true, false),
                AffineECPCoordinate.class);
        SupersingularTateGroup supSingGroupProj = supsingFac.provideBilinearGroup(80,
                new BilinearGroupRequirement(BilinearGroup.Type.TYPE_1, true, true, false),
                ProjectiveECPCoordinate.class);

        // BN curves
        BarretoNaehrigProvider bnFac = new BarretoNaehrigProvider();
        //BarretoNaehrigBilinearGroup bnGroupAffine = bnFac.provideBilinearGroup(128,
        //        new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3, true, true, false),
        //        AffineECPCoordinate::new);
        //BarretoNaehrigBilinearGroup bnGroupProj = bnFac.provideBilinearGroup(128,
        //        new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3, true, true, false),
        //        ProjectiveECPCoordinate::new);

        //Lazy bn curve
        LazyPairing lazyPairing = new LazyPairing(supSingGroupAffine.getBilinearMap());

        // Collect parameters
        BilinearMap[][] params = new BilinearMap[][]{
                {debugMap1}, {debugMap2}, {debugMap3},
        //        {supSingGroupAffine.getBilinearMap()},
                {supSingGroupProj.getBilinearMap()},
        //        {bnGroupAffine.getBilinearMap()},
        //        {bnGroupProj.getBilinearMap()},
                {lazyPairing}};
        return Arrays.asList(params);
    }
}
