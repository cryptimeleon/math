package org.cryptimeleon.math.structures.rings.cartesian;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.structures.rings.RingElement;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

public class ProductRing implements Ring {
    @Represented
    protected Ring[] rings;

    public ProductRing(Ring... rings) {
        this.rings = rings;
    }

    public ProductRing(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return Arrays.stream(rings).map(Ring::size).reduce(BigInteger.ZERO, (s, s2) -> s == null || s2 == null ? null : s.multiply(s2));
    }

    @Override
    public RingElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return new ProductRingElement(Arrays.stream(rings).map(Ring::getUniformlyRandomElement).toArray(RingElement[]::new));
    }

    @Override
    public BigInteger getCharacteristic() throws UnsupportedOperationException {
        BigInteger gcd = BigInteger.ZERO; //greatest common divisor of all characteristics (after for loop)
        BigInteger product = BigInteger.ONE; //product of all characteristics
        for (Ring ring : rings) {
            BigInteger characteristic = ring.getCharacteristic();
            gcd = gcd.gcd(characteristic);
            product = product.multiply(characteristic);
        }

        return product.divide(gcd); //least common multiple of all the characteristics
    }

    @Override
    public RingElement getElement(BigInteger i) {
        return new ProductRingElement(Arrays.stream(rings).map(ring -> ring.getElement(i)).toArray(RingElement[]::new));
    }

    @Override
    public BigInteger sizeUnitGroup() throws UnsupportedOperationException {
        return Arrays.stream(rings).map(Ring::sizeUnitGroup).reduce(BigInteger.ONE, BigInteger::multiply);
    }

    @Override
    public RingElement getZeroElement() {
        return new ProductRingElement(Arrays.stream(rings).map(Ring::getZeroElement).toArray(RingElement[]::new));
    }

    @Override
    public RingElement getOneElement() {
        return new ProductRingElement(Arrays.stream(rings).map(Ring::getOneElement).toArray(RingElement[]::new));
    }

    @Override
    public RingElement restoreElement(Representation repr) {
        return new ProductRingElement(repr);
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        Optional<Integer> result = Optional.of(0);
        for (Ring ring : rings) {
            Optional<Integer> ubl = ring.getUniqueByteLength();
            if (ubl.isPresent())
                result.map(s -> s + ubl.get());
            else
                result = Optional.empty();
        }

        return result;
    }

    @Override
    public boolean isCommutative() {
        return Arrays.stream(rings).allMatch(Ring::isCommutative);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public static ProductRingElement valueOf(RingElement... elems) {
        return new ProductRingElement(elems);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ProductRing)) {
            return false;
        }
        ProductRing otherRing = (ProductRing) other;
        if (this.rings.length != otherRing.rings.length) {
            return false;
        }
        for (int i = 0; i < this.rings.length; ++i) {
            if (!this.rings[i].equals(otherRing.rings[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.rings);
    }
}
