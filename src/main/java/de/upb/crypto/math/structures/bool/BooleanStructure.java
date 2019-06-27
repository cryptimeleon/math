package de.upb.crypto.math.structures.bool;

import com.sun.org.apache.xpath.internal.operations.Bool;
import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.interfaces.structures.Structure;
import de.upb.crypto.math.random.interfaces.RandomGenerator;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;
import java.util.Optional;

public class BooleanStructure implements Ring {

    @Override
    public BigInteger sizeUnitGroup() throws UnsupportedOperationException {
        return BigInteger.ONE;
    }

    @Override
    public RingElement getZeroElement() {
        return BooleanElement.FALSE;
    }

    @Override
    public RingElement getOneElement() {
        return BooleanElement.TRUE;
    }

    @Override
    public RingElement getElement(Representation repr) {
        return new BooleanElement(repr);
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return Optional.of(1);
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return BigInteger.valueOf(2);
    }

    @Override
    public RingElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return RandomGeneratorSupplier.getRnd().nextBit() ? BooleanElement.TRUE : BooleanElement.FALSE;
    }

    @Override
    public BigInteger getCharacteristic() throws UnsupportedOperationException {
        return BigInteger.valueOf(2);
    }

    @Override
    public RingElement getElement(BigInteger i) {
        return i.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO) ? BooleanElement.FALSE : BooleanElement.TRUE;
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    @Override
    public Representation getRepresentation() {
        return new ObjectRepresentation();
    }
}
