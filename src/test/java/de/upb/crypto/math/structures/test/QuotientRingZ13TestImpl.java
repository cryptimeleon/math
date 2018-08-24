package de.upb.crypto.math.structures.test;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.integers.IntegerElement;
import de.upb.crypto.math.structures.integers.IntegerRing;
import de.upb.crypto.math.structures.quotient.PrincipalIdeal;
import de.upb.crypto.math.structures.quotient.QuotientRing;

import java.math.BigInteger;
import java.util.Optional;

/**
 * A very quick implementation of the integer ring modulo 13 using the QuotientRing class.
 * <p>
 * This only serves as a test case implementation for QuotientRing
 */
public class QuotientRingZ13TestImpl extends QuotientRing<RingElement> {
    public QuotientRingZ13TestImpl(Representation repr) {
        super(repr);
    }

    public QuotientRingZ13TestImpl() {
        super(new IntegerRing(), new PrincipalIdeal(new IntegerElement(13)));
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return BigInteger.valueOf(13);
    }

    @Override
    public BigInteger sizeUnitGroup() throws UnsupportedOperationException {
        return BigInteger.valueOf(12);
    }

    @Override
    public RingElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return createElement(new IntegerElement(RandomGeneratorSupplier.getRnd().getRandomElement(BigInteger.valueOf(13))));
    }

    @Override
    public BigInteger getCharacteristic() throws UnsupportedOperationException {
        return size();
    }

    @Override
    public RingElement createElement(RingElement representative) {
        return new QuotientRingElement(representative) {
            @Override
            public Ring getStructure() {
                return QuotientRingZ13TestImpl.this;
            }

            @Override
            public BigInteger getRank() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean divides(RingElement e) throws UnsupportedOperationException {
                return e.isZero() == this.isZero();
            }

            @Override
            public RingElement[] divideWithRemainder(RingElement e) throws UnsupportedOperationException, IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            protected void reduce() {
                representative = new IntegerElement(((IntegerElement) representative).getBigInt().mod(BigInteger.valueOf(13)));
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
                return representative.updateAccumulator(accumulator);
            }
        };
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return Optional.empty(); //can be improved, but not necessary for this test class.
    }

    @Override
    public boolean isCommutative() {
        return true;
    }
}
