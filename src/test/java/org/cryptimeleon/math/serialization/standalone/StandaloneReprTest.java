package org.cryptimeleon.math.serialization.standalone;

import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
 * whose task is essentially to instantiate classes so that they can be tested. <br>
 *
 * To use this test for your own package, extend this class and and set up a no-argument constructor like this: <br>
 * {@code public YourOwnStandaloneReprTest() { super("com.mypackageprefix"); }}
 *
 * @see #StandaloneReprTest(String, String...).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //required for the @MethodSource non-static method to work.
public abstract class StandaloneReprTest {
    protected static HashSet<Class<? extends StandaloneRepresentable>> testedClasses = new HashSet<>();
    private final String packageToTest;
    private final Reflections reflection;

    /**
     * Instantiates the test
     * @param packagePrefixToTest the package (-prefix), for example "com.yourpackageprefix".
     * @param additionalPackagesToScan additional packages (/prefixes) that contain superclasses/interfaces of your StandaloneRepresentable classes.
     *                       When in doubt, add all dependencies of your software that themselves depend on cryptimeleon.
     *                       There is no need to add org.cryptimeleon packages to this list (as they are implicitly added).
     */
    public StandaloneReprTest(String packagePrefixToTest, String... additionalPackagesToScan) {
        this.packageToTest = packagePrefixToTest;
        String[] allPackages = Arrays.copyOf(additionalPackagesToScan, additionalPackagesToScan.length + 2);
        allPackages[additionalPackagesToScan.length] = packagePrefixToTest;
        allPackages[additionalPackagesToScan.length + 1] = "org.cryptimeleon";
        this.reflection = new Reflections((Object[]) allPackages);
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
                .filter(clazz -> clazz.getPackage().getName().startsWith(packageToTest)) //only use those that belong to the desired package
                .map(clazz -> {
                    try {
                        return clazz.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                        e.printStackTrace();
                        return null;
                    } catch (InvocationTargetException e) {
                        if (e.getCause() instanceof RuntimeException)
                            throw (RuntimeException) e.getCause();
                        else
                            throw new RuntimeException("An exception was thrown in the constructor of "+clazz.getSimpleName(), e);
                    }
                })
                .filter(Objects::nonNull)
                .map(Arguments::of);
    }

    protected class TestForParameterlessConstructorClasses extends StandaloneReprSubTest {
        public void testClassesWithTrivialConstructor() {
            reflection.getSubTypesOf(StandaloneRepresentable.class).stream()
                    .filter(clazz -> clazz.getPackage().getName().startsWith(packageToTest)) //only use those that belong to the desired package
                    .filter(clazz -> Arrays.stream(clazz.getConstructors()).anyMatch(constr -> constr.getParameterCount() == 0))
                    .filter(clazz -> Modifier.isPublic(clazz.getModifiers()))
                    .forEach(clazz -> {
                        try {
                            test(clazz.getConstructor().newInstance());
                        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                            fail(e);
                        } catch (InvocationTargetException e) {
                            fail("Exception was thrown during constructor invocation of "+clazz.getSimpleName(), e.getCause());
                        }
                    });
        }
    }

    @AfterAll
    @DisplayName("Checking that every StandaloneRepresentable has been tested.")
    public void checkForUntestedClasses() {
        Set<Class<? extends StandaloneRepresentable>> classesToTest = reflection.getSubTypesOf(StandaloneRepresentable.class);
        classesToTest.removeAll(testedClasses);

        // Remove interfaces and stuff from other packages, and classes that are not public
        classesToTest.removeIf(c -> c.isInterface() || Modifier.isAbstract(c.getModifiers())
                || !c.getPackage().getName().startsWith(packageToTest) || !Modifier.isPublic(c.getModifiers()));

        for (Class<? extends StandaloneRepresentable> notTestedClass : classesToTest) {
            System.err.println(notTestedClass.getName() + " implements StandaloneRepresentable but was not tested by StandaloneTest (or the test failed). You need to define a StandaloneReprSubTest for it.");
        }

        assertTrue(classesToTest.isEmpty(), "Missing (or failed) StandaloneRepresentation tests for "+classesToTest.stream().map(Class::getSimpleName).sorted().collect(Collectors.joining(", ")));
    }
}
