package org.cryptimeleon.math.serialization.annotations;


import org.cryptimeleon.math.random.RandomGenerator;
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
    @Represented
    byte[] smallNumbers;
    @Represented
    Integer boringOldInteger;
    @Represented
    Long longNUmber;
    @Represented
    BigInteger reeeaaallyLongNumber;
    @Represented
    UUID veryUniqueNumber;


    public static class Foo implements StandaloneRepresentable { //for testing restoration nof zpelem with a complicated restorer string
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

    private void populate() {
        Ring ring  = new Zn(BigInteger.TEN);

        nestedMap = new HashMap<>();
        Map<String, RingElement> inner = new HashMap<>();
        inner.put("testInner", ring.getUniformlyRandomElement());
        nestedMap.put(inner, Arrays.asList("testOuter", "testOuter2"));

        foo = new Foo(new Zp(BigInteger.valueOf(3)));
        zpelem = Zp.valueOf(2, 3);

        smallNumbers = RandomGenerator.getRandomBytes(3);
        boringOldInteger = 23;
        longNUmber = Long.MAX_VALUE;
        reeeaaallyLongNumber = BigInteger.TEN.pow(100);
        veryUniqueNumber = UUID.randomUUID();
    }

    @Test
    public void testRestoration() {
        Ring ring  = new Zn(BigInteger.TEN);

        Representation repr = ReprUtil.serialize(this);
        ReprUtilTest deserialized = new ReprUtilTest();
        new ReprUtil(deserialized).register(ring, "R").deserialize(repr);

        assertEquals(deserialized, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReprUtilTest that = (ReprUtilTest) o;
        return Objects.equals(nestedMap, that.nestedMap) && Objects.equals(zpelem, that.zpelem) && Objects.equals(foo, that.foo) && Arrays.equals(smallNumbers, that.smallNumbers) && Objects.equals(boringOldInteger, that.boringOldInteger) && Objects.equals(longNUmber, that.longNUmber) && Objects.equals(reeeaaallyLongNumber, that.reeeaaallyLongNumber) && Objects.equals(veryUniqueNumber, that.veryUniqueNumber);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(nestedMap, zpelem, foo, boringOldInteger, longNUmber, reeeaaallyLongNumber, veryUniqueNumber);
        result = 31 * result + Arrays.hashCode(smallNumbers);
        return result;
    }
}
