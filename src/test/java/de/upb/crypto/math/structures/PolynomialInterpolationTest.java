package de.upb.crypto.math.structures;

import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.random.interfaces.RandomGenerator;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.structures.polynomial.PolynomialRing;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class PolynomialInterpolationTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private static final int RING_SIZE = 10;

    private RandomGenerator rng;
    private Ring ring;

    @Before
    public void setup() {
        rng = RandomGeneratorSupplier.getRnd();
        ring = new Zp(rng.getRandomPrime(RING_SIZE));
    }


    @Test
    public void testPolynomialWithRandomCoefficients() {
        //choose a desired degree
        int degree = rng.getRandomElement(5).intValue() + 5;

        //Take degree + 1 coefficient to form the reference polynomial
        RingElement[] coefficients = Stream.generate(ring::getUniformlyRandomElement)
                .distinct()
                .limit(degree + 1)
                .toArray(RingElement[]::new);

        PolynomialRing.Polynomial polynomial = PolynomialRing.getPoly(coefficients);

        //Take degree + 1 data points for the interpolation
        Map<RingElement, RingElement> dataPoints = Stream.generate(ring::getUniformlyRandomElement)
                .distinct()
                .limit(degree + 1)
                .collect(Collectors.toMap(Function.identity(), polynomial::evaluate));

        //Interpolate the polynomial from the chosen data points
        PolynomialRing.Polynomial interpolatedPoly = PolynomialRing.getPoly(dataPoints, degree);

        //Ensure that all data points are actually on the interpolated polynomial
        for (Map.Entry<RingElement, RingElement> dataPoint : dataPoints.entrySet()) {
            assertEquals(dataPoint.getValue(), interpolatedPoly.evaluate(dataPoint.getKey()));
        }

        assertArrayEquals(coefficients, interpolatedPoly.getCoefficients());
        assertEquals(polynomial, interpolatedPoly);
    }

    /**
     * Invalid parameters for the polynomial creation should always throw an IllegalArgumentException.
     * Test cases:
     * - data points {@code Map} is {@code null}
     * - too few data points to create a polynomial of preferred degree
     * - preferred degree is negative
     */
    @Test
    public void testInterpolationExceptionHandling() {
        exception.expect(IllegalArgumentException.class);
        PolynomialRing.getPoly(null, 0);
        exception.expect(IllegalArgumentException.class);
        PolynomialRing.getPoly(Collections.emptyMap(), 0);

        Map<RingElement, RingElement> dataPoints = Stream.generate(ring::getUniformlyRandomElement)
                .distinct()
                .limit(1)
                .collect(Collectors.toMap(Function.identity(), Function.identity()));

        exception.expect(IllegalArgumentException.class);
        PolynomialRing.getPoly(dataPoints, 0);

        exception.expect(IllegalArgumentException.class);
        PolynomialRing.getPoly(dataPoints, -1);

    }
}
