package de.upb.crypto.math.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingGroup;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.pairings.debug.count.CountingBilinearGroup;
import de.upb.crypto.math.pairings.debug.count.CountingBilinearGroupProvider;
import de.upb.crypto.math.pairings.debug.count.CountingGroup;
import de.upb.crypto.math.structures.groups.basic.BasicGroup;
import de.upb.crypto.math.structures.groups.basic.BasicGroupElement;
import de.upb.crypto.math.structures.groups.exp.ExponentiationAlgorithms;
import de.upb.crypto.math.structures.groups.exp.MultiExpTerm;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;
import de.upb.crypto.math.structures.groups.lazy.LazyGroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManualTest {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);

        BilinearGroup bilGroup = fac.createBilinearGroup();
        for (int i = 0; i < 10; ++i) {
            Multiexponentiation multiexponentiation = genMultiExp(bilGroup.getG1(), 10);
            assertEquals(ExponentiationAlgorithms.interleavingSlidingWindowMultiExpA1(multiexponentiation, 4),
                    ExponentiationAlgorithms.interleavingSlidingWindowMultiExpA2(multiexponentiation, 4));
            assertEquals(ExponentiationAlgorithms.interleavingWnafMultiExp(multiexponentiation, 4),
                    ExponentiationAlgorithms.interleavingSlidingWindowMultiExpA1(multiexponentiation, 4));
            assertEquals(ExponentiationAlgorithms.interleavingWnafMultiExp(multiexponentiation, 4),
                    ExponentiationAlgorithms.interleavingSlidingWindowMultiExpA2(multiexponentiation, 4));
        }
    }

    public static Multiexponentiation genMultiExp(Group group, int numTerms) throws NoSuchMethodException,
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
                            BigInteger.valueOf(rand.nextLong())
                    )
            );
        }
        return multiexponentiation;
    }
}
