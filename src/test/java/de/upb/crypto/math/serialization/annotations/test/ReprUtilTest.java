package de.upb.crypto.math.serialization.annotations.test;


import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
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
    @Represented(restorer = "foo::getFoo::getZp")
    Zp.ZpElement zpelem;
    @Represented
    Foo foo;

    public static class Foo implements StandaloneRepresentable {
        @Represented
        Zp zp;

        public Foo(Zp zp) {
            this.zp = zp;
        }

        public Foo(Representation repr) {

        }

        public Zp getZp() {
            return new Zp(BigInteger.valueOf(3));
        }

        public Foo getFoo() {
            return this;
        }

        @Override
        public Representation getRepresentation() {
            return null;
        }
    }

    @Test
    public void testNestedMap() {
        Ring ring  = new Zn(BigInteger.TEN);

        HashMap<Map<String, RingElement>, List<String>> nestedMapOriginal = new HashMap<>();
        Map<String, RingElement> inner = new HashMap<>();
        inner.put("testInner", ring.getUniformlyRandomElement());
        nestedMap = nestedMapOriginal;

        nestedMapOriginal.put(inner, Arrays.asList("testOuter", "testOuter2"));

        foo = new Foo(new Zp(BigInteger.valueOf(3)));
        zpelem = Zp.valueOf(2, 3);

        Representation repr = ReprUtil.serialize(this);
        nestedMap = null;
        zpelem = null;
        foo = null;
        new ReprUtil(this).register(ring, "R").deserialize(repr);

        assertEquals(nestedMapOriginal, nestedMap);
        assertEquals(Zp.valueOf(2,3), zpelem);
    }
}
