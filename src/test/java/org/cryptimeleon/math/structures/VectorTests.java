package org.cryptimeleon.math.structures;

import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VectorTests {

    /**
     * Test that concatenation yields the correct result.
     */
    @Test
    void testConcatenation() {
        Vector<Integer> firstVector = Vector.of(0, 1, 2, 3);
        Vector<Integer> secondVector = Vector.of(4, 5, 6);
        Vector<Integer> concatenation = firstVector.concatenate(secondVector);

        assertEquals(concatenation.length(), firstVector.length() + secondVector.length());
        for (int i = 0; i < concatenation.length(); i++)
            assertEquals(concatenation.get(i), i);
    }
}
