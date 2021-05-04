package org.cryptimeleon.math.structures;

import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.GroupImpl;
import org.cryptimeleon.math.structures.groups.debug.DebugBilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.exp.ExponentiationAlgorithms;
import org.cryptimeleon.math.structures.groups.exp.MultiExpTerm;
import org.cryptimeleon.math.structures.groups.exp.Multiexponentiation;
import org.cryptimeleon.math.structures.groups.exp.SmallExponentPrecomputation;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpTests {

    @Test
    public void testMultiExpAlgs() {
        BilinearGroupImpl bilGroup = new DebugBilinearGroupImpl(60, BilinearGroup.Type.TYPE_3);
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
        Multiexponentiation multiexponentiation = new Multiexponentiation();
        for (int i = 0; i < numTerms; ++i) {
            multiexponentiation.put(
                    new MultiExpTerm(
                            group.getUniformlyRandomNonNeutral(),
                            RandomGenerator.getRandomNumber(BigInteger.valueOf(Integer.MAX_VALUE))
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
        BilinearGroupImpl bilGroup = new DebugBilinearGroupImpl(60, BilinearGroup.Type.TYPE_3);
        for (int i = 0; i < 4; ++i) {
            GroupElementImpl elem = bilGroup.getG1().getUniformlyRandomNonNeutral();
            BigInteger exponent = RandomGenerator.getRandomNumber(BigInteger.valueOf(Integer.MAX_VALUE));
            //System.out.println("Chosen element: " + elem);
            //System.out.println("Chosen exponent: " + exponent);
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
