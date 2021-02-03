package de.upb.crypto.math.hash.annotations;

import de.upb.crypto.math.hash.ByteAccumulator;
import de.upb.crypto.math.hash.UniqueByteRepresentable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertNotEquals;

public class AnnotatedUbrUtilTest {

    public class Testclass implements UniqueByteRepresentable {
        @UniqueByteRepresented
        ArrayList<String> testlist;
        @UniqueByteRepresented
        HashMap<String, byte[]> testMap;

        public Testclass(String... testlist) {
            this.testlist = new ArrayList<>(Arrays.asList(testlist));
        }
        public Testclass(HashMap<String, byte[]> testMap) { this.testMap = testMap; }

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

        HashMap<String, byte[]> map1 = new HashMap<>();
        map1.put("bla", new byte[] {0, 1, 2, 3});
        map1.put("blubb", new byte[] {0});

        HashMap<String, byte[]> map2 = new HashMap<>();
        map2.put("bla", new byte[] {0, 1, 2, 3, 4});
        map2.put("blubb", new byte[] {0});

        a = new Testclass(map1);
        b = new Testclass(map2);
        assertNotEquals(a.getUniqueByteRepresentation(), b.getUniqueByteRepresentation());
        System.out.println(Arrays.toString(a.getUniqueByteRepresentation()));
        System.out.println(Arrays.toString(b.getUniqueByteRepresentation()));
    }
}
