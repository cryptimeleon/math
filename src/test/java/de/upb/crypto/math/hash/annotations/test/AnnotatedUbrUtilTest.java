package de.upb.crypto.math.hash.annotations.test;

import de.upb.crypto.math.hash.annotations.AnnotatedUbrUtil;
import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertNotEquals;

public class AnnotatedUbrUtilTest {

    public class Testclass implements UniqueByteRepresentable {
        ArrayList<String> testlist;

        public Testclass(String... testlist) {
            this.testlist = new ArrayList<>(Arrays.asList(testlist));
        }

        @Override
        public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
            return AnnotatedUbrUtil.autoAccumulate(accumulator, this);
        }
    }

    @Test
    public void testUniqueness() {
        Testclass a = new Testclass("xy", "z");
        Testclass b = new Testclass("x", "yz");

        assertNotEquals(a.getUniqueByteRepresentation(), b.getUniqueByteRepresentation());
    }
}
