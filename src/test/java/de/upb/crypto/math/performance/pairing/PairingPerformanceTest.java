package de.upb.crypto.math.performance.pairing;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigProvider;
import de.upb.crypto.math.pairings.supersingular.SupersingularProvider;
import de.upb.crypto.math.structures.zn.Zn;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class PairingPerformanceTest {
    private BilinearMap pairing;

    private ArrayList<GroupElement> g1Elements;
    private ArrayList<GroupElement> g2Elements;
    private ArrayList<BigInteger> exponents;
    GroupElementExpression expression;

    final int numberOfElements = 1;

    public PairingPerformanceTest(BilinearMap pairing) {
        this.pairing = pairing;
    }

    /*
     * Add pairings to test here.
     */
    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<BilinearMap> initializePairings() {
        ArrayList<BilinearMap> pairings = new ArrayList<>();

        // Supersingular Tate Pairing
        SupersingularProvider supersingularProvider = new SupersingularProvider();
        pairings.add(supersingularProvider.provideBilinearGroup(80,
                new BilinearGroupRequirement(BilinearGroup.Type.TYPE_1)).getBilinearMap());

        // Barreto-Naehrig non-native
        BarretoNaehrigProvider bnProvider = new BarretoNaehrigProvider();
        pairings.add(bnProvider.provideBilinearGroup(128,
                new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3)).getBilinearMap());
        // Barreto-Naehrig non-native, SFC-256
        pairings.add(
                bnProvider.provideBilinearGroupFromSpec(BarretoNaehrigProvider.ParamSpecs.SFC256).getBilinearMap());

        return pairings;
    }

    @Before
    public void setupTest() {
        Group g1 = pairing.getG1();
        Group g2 = pairing.getG2();
        expression = pairing.expr();

        // Generate test data
        g1Elements = new ArrayList<>();
        g2Elements = new ArrayList<>();
        exponents = new ArrayList<>();

        for (int i = 0; i < numberOfElements; i++) {
            g1Elements.add(g1.getUniformlyRandomElement());
            g2Elements.add(g2.getUniformlyRandomElement());
            exponents.add(new Zn(g1.size()).getUniformlyRandomElement().getInteger());
            expression.opPow(pairing.expr(g1Elements.get(i), g2Elements.get(i)), exponents.get(i));
        }

        System.out.println("Testing " + pairing.getClass().getSimpleName() + " with " + numberOfElements + " pairings...");
    }


    @Test
    public void evaluatePairing() {
        long referenceTime = System.nanoTime();
        GroupElement result = this.expression.evaluate();
        System.out.println("Time to evaluate: " + (System.nanoTime() - referenceTime) / 1e6 + " ms");

        referenceTime = System.nanoTime();
        for (int i = 0; i < numberOfElements*10; i++) {
            pairing.apply(this.g1Elements.get(0), this.g2Elements.get(0));
        }
        System.out.println("Time to evaluate: " + (System.nanoTime() - referenceTime) / 1e6 / (numberOfElements * 10)  + " ms");

    }
}
