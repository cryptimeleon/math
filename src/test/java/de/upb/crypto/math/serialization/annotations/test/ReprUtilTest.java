package de.upb.crypto.math.serialization.annotations.test;


import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.zn.Zn;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ReprUtilTest {
    @Represented(restorer = "(Str -> R) -> [Str]")
    HashMap<Map<String, RingElement>, List<String>> nestedMap;

    @Test
    public void testNestedMap() {
        Ring ring  = new Zn(BigInteger.TEN);

        HashMap<Map<String, RingElement>, List<String>> nestedMapOriginal = new HashMap<>();
        Map<String, RingElement> inner = new HashMap<>();
        inner.put("testInner", ring.getUniformlyRandomElement());
        nestedMap = nestedMapOriginal;

        nestedMapOriginal.put(inner, Arrays.asList("testOuter"));

        Representation repr = ReprUtil.serialize(this);
        nestedMap = null;
        new ReprUtil(this).register(ring, "R").deserialize(repr);

        assertEquals(nestedMapOriginal, nestedMap);
    }
}
