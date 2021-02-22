package org.cryptimeleon.math.structures;

import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.GroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.type3.bn.BarretoNaehrigBilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.exp.ExponentiationAlgorithms;
import org.cryptimeleon.math.structures.groups.exp.MultiExpTerm;
import org.cryptimeleon.math.structures.groups.exp.Multiexponentiation;
import org.cryptimeleon.math.structures.groups.exp.SmallExponentPrecomputation;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpTests {

    @Test
    public void testMultiExpAlgs() {
        BilinearGroupImpl bilGroup = new BarretoNaehrigBilinearGroupImpl(60);
        for (int i = 0; i < 10; ++i) {
            Multiexponentiation multiexponentiation = genMultiExp(bilGroup.getG1(), 10);
            System.out.println(multiexponentiation);
            GroupElementImpl naiveResult = naiveEval(multiexponentiation);
            GroupElementImpl slidingResult = ExponentiationAlgorithms
                    .interleavingSlidingWindowMultiExp(multiexponentiation, 4);
            GroupElementImpl wNafResult = ExponentiationAlgorithms
                    .interleavingWnafMultiExp(multiexponentiation, 4);
            assertEquals(naiveResult, wNafResult);
            assertEquals(naiveResult, slidingResult);
        }
    }

    private static Multiexponentiation genMultiExp(GroupImpl group, int numTerms) {
        SecureRandom secRand = new SecureRandom();
        Random rand = new Random(secRand.nextLong());
        Multiexponentiation multiexponentiation = new Multiexponentiation();
        for (int i = 0; i < numTerms; ++i) {
            multiexponentiation.put(
                    new MultiExpTerm(
                            group.getUniformlyRandomNonNeutral(),
                            BigInteger.valueOf(rand.nextInt())
                    )
            );
        }
        return multiexponentiation;
    }

    private static GroupElementImpl naiveEval(Multiexponentiation multiexp) {
        GroupElementImpl result = multiexp.getTerms().get(0).getBase().getStructure().getNeutralElement();
        for (MultiExpTerm term : multiexp.getTerms()) {
            result = result.op(term.getBase().pow(term.getExponent()));
        }
        return result;
    }

    @Test
    public void testExpAlgs() {
        BilinearGroupImpl bilGroup = new BarretoNaehrigBilinearGroupImpl(60);

        SecureRandom secRand = new SecureRandom();
        Random rand = new Random(secRand.nextLong());
        for (int i = 0; i < 10; ++i) {
            GroupElementImpl elem = bilGroup.getG1().getUniformlyRandomNonNeutral();
            BigInteger exponent = BigInteger.valueOf(rand.nextInt());
            System.out.println("Chosen element: " + elem);
            System.out.println("Chosen exponent: " + exponent);
            GroupElementImpl naiveResult = ExponentiationAlgorithms.binSquareMultiplyExp(elem, exponent);
            GroupElementImpl slidingResult = ExponentiationAlgorithms.slidingWindowExp(
                    elem, exponent, new SmallExponentPrecomputation(elem), 4
            );
            GroupElementImpl wNafResult = ExponentiationAlgorithms.wnafExp(
                    elem, exponent, new SmallExponentPrecomputation(elem), 4
            );
            assertEquals(naiveResult, slidingResult);
            assertEquals(naiveResult, wNafResult);
        }
    }
}
