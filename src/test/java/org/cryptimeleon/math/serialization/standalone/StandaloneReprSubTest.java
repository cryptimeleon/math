package org.cryptimeleon.math.serialization.standalone;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for testing {@link StandaloneRepresentable}s. <br>
 *
 * Implementing classes should write methods that instantiate a {@linkplain StandaloneRepresentable}
 * and then call {@link #test(StandaloneRepresentable)} for it.
 * All public (non-static) methods the implementing class declares are automatically executed at test time.
 * When in doubt, check out some examples (e.g., {@link org.cryptimeleon.math.serialization.standalone.params.StructureStandaloneReprTest}).
 */
public abstract class StandaloneReprSubTest {
    private HashSet<Class<? extends StandaloneRepresentable>> testedClasses;

    protected final void test(StandaloneRepresentable object) {
        Class<? extends StandaloneRepresentable> clazz = object.getClass();
        testedClasses.add(clazz);

        //Test for constructor with single Representation parameter
        try {
            Constructor<? extends StandaloneRepresentable> constructor = clazz.getConstructor(Representation.class);
            assertNotNull(constructor, "Constructor is null");
        } catch (NoSuchMethodException | SecurityException e) {
            // no constructor given or constructor not visible
            fail(clazz.getName() + " has no public constructor with a single Representation parameter");
        }

        // Test for override of equals
        try {
            Method equals = clazz.getMethod("equals", Object.class);
            assertNotEquals(equals.getDeclaringClass(), Object.class);
        } catch (NoSuchMethodException | SecurityException e) {
            fail(clazz.getName() + " does not override equals(Object)");
        }

        // Test for override of hashCode
        try {
            Method hashCode = clazz.getMethod("hashCode");
            assertNotEquals(hashCode.getDeclaringClass(), Object.class);
        } catch (NoSuchMethodException | SecurityException e) {
            fail(clazz.getName() + " does not override hashCode()");
        }

        //Test serialization/deserialization
        try {
            Constructor<? extends StandaloneRepresentable> constructor = clazz.getConstructor(Representation.class);
            assertNotNull(constructor);
            Representation repr = (Representation) clazz.getMethod("getRepresentation").invoke(object);
            assertEquals(object, constructor.newInstance(repr));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
            fail("An exception occured while serializing/deserializing "+clazz.getName());
        }
    }

    public final Set<Class<? extends StandaloneRepresentable>> runTests() {
        //Clear list
        testedClasses = new HashSet<>();

        //Call every non-private method on this object
        Method[] declaredMethods = getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            try {
                if (method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers())) {
                    method.invoke(this);
                }
            } catch (IllegalAccessException e) {
                fail(e);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                fail("Exception thrown during execution of "+getClass().getName()+"::"+method.getName(), e);
            }
        }

        return testedClasses;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
