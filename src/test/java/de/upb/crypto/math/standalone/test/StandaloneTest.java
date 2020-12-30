package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.serialization.annotations.test.ReprUtilTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(value = Parameterized.class)
public class StandaloneTest {

    private final Class<? extends StandaloneRepresentable> toTest;
    private final Object instance;

    public StandaloneTest(StandaloneTestParams params) {
        this.toTest = params.toTest;
        this.instance = params.instance;
    }

    @Test
    public void testForConstructor() {
        // Test for constructor
        try {
            // tries to get the constructor that has Representation as class
            // parameters
            Constructor<? extends StandaloneRepresentable> c = toTest.getConstructor(Representation.class);
            assertTrue(c != null);
        } catch (NoSuchMethodException | SecurityException e) {
            fail("Any StandaloneRepresentable class must provide a constructor with a single 'Representation' parameter");
        }
    }

    @Test
    public void checkForOverrideEquals() {
        // checks if all classes overwrite the equals method
        try {
            Method equals = toTest.getMethod("equals", Object.class);
            // this is maybe not enough since it only asserts that any super
            // class overwrites equals
            assertTrue(!equals.getDeclaringClass().equals(Object.class));
        } catch (NoSuchMethodException | SecurityException e) {
            fail("StandaloneRepresentable classes must implement equals() and hashCode()");
        }
    }

    @Test
    public void checkForOverrideHashCode() {
        try {
            Method hashCode = toTest.getMethod("hashCode");
            // this is maybe not enough since it only asserts that any super
            // class overwrites hashcode
            assertTrue(!hashCode.getDeclaringClass().equals(Object.class));
        } catch (NoSuchMethodException | SecurityException e) {
            fail("StandaloneRepresentable classes must implement equals() and hashCode()");
        }
    }

    @Test
    public void testRecreateRepresentable() {
        // tests whether the deserialization of the serialized object equals the
        // original object
        if (instance == null) {
            System.out.println();
            fail("No object given for " + toTest.getName() + " - add appropriate StandaloneTestParams to the StandaloneTest");
        } else {
            try {
                Constructor<? extends StandaloneRepresentable> c = toTest.getConstructor(Representation.class);
                assertNotNull(c);
                Representation repr = (Representation) toTest.getMethod("getRepresentation").invoke(instance);
                Object recreated = c.newInstance(repr);
                assertEquals("Recreated object isn't equal to the original one", instance, recreated);
                assertEquals("Recreated object doesn't have same hash value as the original one", instance.hashCode(), recreated.hashCode());

            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException
                    | InstantiationException e) {

                e.printStackTrace();
                fail();

            } catch (NoSuchMethodException | SecurityException e2) {
                fail();
            }

        }
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<StandaloneTestParams> getStandaloneClasses() {
        Reflections reflection = new Reflections("de.upb.crypto.math");
        // get all classes that are subtypes of standalone representable
        Set<Class<? extends StandaloneRepresentable>> classes = reflection.getSubTypesOf(StandaloneRepresentable.class);
        ArrayList<StandaloneTestParams> toReturn = new ArrayList<>();
        // add params here
        toReturn.addAll(BarretoNaehrigParams.get());
        toReturn.add(F2FiniteFieldParams.get());
        toReturn.add(FiniteFieldExtensionParams.get());
        toReturn.add(HashIntoZnAdditiveGroupParams.get());
        toReturn.add(HashIntoZnParams.get());
        toReturn.add(HashIntoZpParams.get());
        toReturn.add(IdentityIsomorphismParams.get());
        toReturn.add(IntegerRingParams.get());
        toReturn.add(PolynomialRingParams.get());
        toReturn.addAll(RingGroupParams.get());
        toReturn.addAll(SHAHashParams.get());
        toReturn.addAll(SuperSingularParams.get());
        toReturn.addAll(BasicAndLazyGroupParams.get());
        toReturn.add(ZnParams.get());
        toReturn.add(ZpParams.get());
        toReturn.add(SnParams.get());
        toReturn.add(QuotientRing1Params.get());
        toReturn.add(ExtensionFieldParams.get());
        toReturn.add(VariableOutputLengthHashFunctionParams.get());
        toReturn.add(NullTestParams.get());
        toReturn.add(RepresentedEnumParams.get());
        toReturn.add(BooleanStructureParams.get());
        toReturn.add(ProductRingParams.get());
        toReturn.add(ProductGroupParams.get());
        toReturn.add(ReprUtilTest.Foo.getStandaloneTestParams());
        toReturn.addAll(CountingBilinearGroupParams.get());
        toReturn.add(Secp256k1Params.get());

        // remove all provided params
        for (StandaloneTestParams stp : toReturn) {
            classes.remove(stp.toTest);
        }
        // add remaining classes
        for (Class<? extends StandaloneRepresentable> c : classes) {
            if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
                toReturn.add(new StandaloneTestParams(c, null));
            }
        }
        return toReturn;
    }
}
