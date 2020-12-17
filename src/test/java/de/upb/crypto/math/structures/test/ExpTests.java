package de.upb.crypto.math.structures.test;

import de.upb.crypto.math.pairings.generic.BilinearGroup;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.structures.groups.exp.ExponentiationAlgorithms;
import de.upb.crypto.math.structures.groups.exp.MultiExpTerm;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;
import de.upb.crypto.math.structures.groups.exp.SmallExponentPrecomputation;
import de.upb.crypto.math.structures.groups.lazy.LazyGroupElement;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpTests {

    @Test
    public void testMultiExpAlgs() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);

        BilinearGroup bilGroup = fac.createBilinearGroup();
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

    private static Multiexponentiation genMultiExp(Group group, int numTerms) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        SecureRandom secRand = new SecureRandom();
        Random rand = new Random(secRand.nextLong());
        Multiexponentiation multiexponentiation = new Multiexponentiation();
        Method getValue = LazyGroupElement.class.getDeclaredMethod("getConcreteValue");
        getValue.setAccessible(true);
        for (int i = 0; i < numTerms; ++i) {
            multiexponentiation.put(
                    new MultiExpTerm(
                            (GroupElementImpl) getValue.invoke(group.getUniformlyRandomNonNeutral()),
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
    public void testExpAlgs() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);

        BilinearGroup bilGroup = fac.createBilinearGroup();
        Method getValue = LazyGroupElement.class.getDeclaredMethod("getConcreteValue");
        getValue.setAccessible(true);

        SecureRandom secRand = new SecureRandom();
        Random rand = new Random(secRand.nextLong());
        for (int i = 0; i < 10; ++i) {
            GroupElementImpl elem = (GroupElementImpl) getValue.invoke(bilGroup.getG1().getUniformlyRandomNonNeutral());
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
