package org.cryptimeleon.math.serialization.annotations;


import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zp;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

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

        public Foo() {
            this(new Zp(BigInteger.valueOf(17)));
        }

        public Foo(Zp zp) {
            this.zp = zp;
        }

        public Foo(Representation repr) {
            ReprUtil.deserialize(this, repr);
        }

        public Zp getZp() {
            return new Zp(BigInteger.valueOf(3));
        }

        public Foo getFoo() {
            return this;
        }

        @Override
        public Representation getRepresentation() {
            return ReprUtil.serialize(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Foo foo = (Foo) o;
            return Objects.equals(zp, foo.zp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(zp);
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
