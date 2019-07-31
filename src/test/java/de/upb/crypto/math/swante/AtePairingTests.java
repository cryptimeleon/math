package de.upb.crypto.math.swante;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.lazy.LazyPairing;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigBilinearGroup;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigProvider;
import de.upb.crypto.math.pairings.bn.MyBarretoNaehrigAtePairing;
import de.upb.crypto.math.pairings.debug.DebugBilinearMap;
import de.upb.crypto.math.pairings.generic.AbstractPairing;
import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.supersingular.SupersingularProvider;
import de.upb.crypto.math.pairings.supersingular.SupersingularTateGroup;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.quotient.FiniteFieldExtension;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import static de.upb.crypto.math.swante.util.MyUtil.pln;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * My tests for the new ate pairing
 */
public class AtePairingTests {
    
    
    @Test
    public void testAte() {
        MyBarretoNaehrigAtePairing pairing = MyBarretoNaehrigAtePairing.createAtePairing(128);
//        BigInteger r = pairing.getG1().size();
//        Field structure = ((AbstractEllipticCurvePoint) pairing.getG1().getGenerator()).getX().getStructure();
//        BigInteger p = ((ExtensionField) structure).getBaseField().size();
        testBasicProperties(pairing);
    }
    
    @Test
    public void testTate() {
        BarretoNaehrigProvider bnFac = new BarretoNaehrigProvider();
        BarretoNaehrigBilinearGroup bnGroup = bnFac.provideBilinearGroup(128, new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3, true, true, false));
        BilinearMap pairing = bnGroup.getBilinearMap();
        testBasicProperties((AbstractPairing) pairing);
    }
    
    public void testBasicProperties(AbstractPairing pairing) {
        BigInteger r = pairing.getG1().size();
        Assert.assertEquals(r, pairing.getG2().size());
        Assert.assertEquals(r, pairing.getGT().size());
        Assert.assertEquals(pairing.getG1().getGenerator().pow(r), pairing.getG1().getNeutralElement());
        Assert.assertEquals(pairing.getG2().getGenerator().pow(r), pairing.getG2().getNeutralElement());
        Assert.assertEquals(pairing.getGT().getGenerator().pow(r), pairing.getGT().getNeutralElement());
        
        
        GroupElement p1 = pairing.getG1().getUniformlyRandomElement(), r1 = pairing.getG1().getUniformlyRandomElement();
        GroupElement p2 = pairing.getG2().getUniformlyRandomElement(), r2 = pairing.getG2().getUniformlyRandomElement();;
        
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
        System.out.println(p1);
        System.out.println(p2);
        System.out.println(t3);
        System.out.println(t4);
        
        //Bilinearity in second argument
        // e(R1,P2+R2)e(-R1,P2)e(-R1,R2)=1
        assertTrue("Bilinearity in second argument", pairing.apply(r1, p2.op(r2)).op(pairing.apply(r1.inv(), p2)).op(pairing.apply(r1.inv(), r2)).isNeutralElement());
        
        //Basic other properties (neutral point paired with any other point gives neutral element in target group)
        assertTrue(pairing.apply(pairing.getG1().getNeutralElement(), p2).isNeutralElement());
        assertTrue(pairing.apply(p1, pairing.getG2().getNeutralElement()).isNeutralElement());
        
        //e(x1*P1,x2*P2) = e(P1,P2)^{x1*x2}
        Zn zn = new Zn(pairing.getG1().size());
        Zn.ZnElement x1 = zn.getUniformlyRandomElement(), x2 = zn.getUniformlyRandomElement();
        assertTrue(pairing.apply(p1.pow(x1), p2.pow(x2)).equals(pairing.apply(p1, p2).pow(x1.mul(x2))));
    }
    
    
}
