package de.upb.crypto.math.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingGroup;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.pairings.debug.count.CountingBilinearGroup;
import de.upb.crypto.math.pairings.debug.count.CountingBilinearGroupProvider;
import de.upb.crypto.math.pairings.debug.count.CountingGroup;
import de.upb.crypto.math.structures.groups.basic.BasicGroup;
import de.upb.crypto.math.structures.groups.basic.BasicGroupElement;
import de.upb.crypto.math.structures.groups.exp.ExponentiationAlgorithms;
import de.upb.crypto.math.structures.groups.lazy.LazyGroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManualTest {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        Method getValue = LazyGroupElement.class.getDeclaredMethod("getConcreteValue");
        getValue.setAccessible(true);
        BilinearGroup bilGroup = fac.createBilinearGroup();
        for (int i = -500; i < 500; ++i) {
            GroupElementImpl elem = (GroupElementImpl) getValue.invoke(bilGroup.getG1().getUniformlyRandomElement());
            GroupElementImpl correctResult = ExponentiationAlgorithms.binSquareMultiplyExp(elem, BigInteger.valueOf(i));
            assertEquals(
                    ExponentiationAlgorithms.slidingWindowExpA1(elem, BigInteger.valueOf(i), null, 4),
                    correctResult
            );
            assertEquals(
                    ExponentiationAlgorithms.slidingWindowExpA2(elem, BigInteger.valueOf(i), null, 4),
                    correctResult
            );
            assertEquals(
                    ExponentiationAlgorithms.slidingWindowExpA3(elem, BigInteger.valueOf(i), null, 4),
                    correctResult
            );
        }
    }
}
