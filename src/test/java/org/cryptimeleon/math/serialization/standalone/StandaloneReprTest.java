package org.cryptimeleon.math.serialization.standalone;

import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests that {@link StandaloneRepresentable} classes fulfill their contract.<br>
 * This class mostly just gathers the classes that need testing and delegates to {@link StandaloneReprSubTest},
 * whose task is essentially to instantiate classes so that they can be tested.
 *
 * To use this test for your own package, extend this class and and set up a no-argument constructor like this: <br>
 * {@code public YourOwnStandaloneReprTest() { super("com.mypackageprefix"); }}
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //required for the @MethodSource non-static method to work.
public abstract class StandaloneReprTest {
    protected static HashSet<Class<? extends StandaloneRepresentable>> testedClasses = new HashSet<>();
    private final String packageName;
    private final Reflections reflection;

    public StandaloneReprTest(String packageName) {
        this.packageName = packageName;
        this.reflection = new Reflections(packageName);
    }

    @ParameterizedTest(name = "''{0}''")
    @MethodSource("provideStandaloneReprSubTests")
    public void testStandaloneRepresentables(StandaloneReprSubTest subtest) {
        testedClasses.addAll(subtest.runTests());
    }

    @Test
    public void testStandaloneRepresentablesWithParameterlessConstructors() {
        testedClasses.addAll(new TestForParameterlessConstructorClasses().runTests());
    }

    protected Stream<? extends Arguments> provideStandaloneReprSubTests() {
        return reflection.getSubTypesOf(StandaloneReprSubTest.class).stream()
                .filter(clazz -> !clazz.equals(TestForParameterlessConstructorClasses.class))
                .map(clazz -> {
                    try {
                        return clazz.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(Arguments::of);
    }

    protected class TestForParameterlessConstructorClasses extends StandaloneReprSubTest {
        public void testClassesWithTrivialConstructor() {
            reflection.getSubTypesOf(StandaloneRepresentable.class).stream()
                    .filter(clazz -> Arrays.stream(clazz.getConstructors()).anyMatch(constr -> constr.getParameterCount() == 0))
                    .forEach(clazz -> {
                        try {
                            test(clazz.getConstructor().newInstance());
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            fail(e);
                        }
                    });
        }
    }

    @AfterAll
    @DisplayName("Checking that every StandaloneRepresentable has been tested.")
    public void checkForUntestedClasses() {
        Set<Class<? extends StandaloneRepresentable>> classesToTest = reflection.getSubTypesOf(StandaloneRepresentable.class);
        classesToTest.removeAll(testedClasses);

        //Remove interfaces and such
        classesToTest.removeIf(c -> c.isInterface() || Modifier.isAbstract(c.getModifiers()) || !c.getPackage().toString().startsWith("package "+packageName));

        for (Class<? extends StandaloneRepresentable> notTestedClass : classesToTest) {
            System.err.println(notTestedClass.getName() + " implements StandaloneRepresentable was not tested by StandaloneTest. You need to define a StandaloneSubTest for it.");
        }

        assertTrue(classesToTest.isEmpty(), "Missing (or failed) StandaloneRepresentation tests for "+classesToTest.stream().map(Class::getSimpleName).sorted().collect(Collectors.joining(", ")));
    }
}
