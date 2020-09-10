package de.upb.crypto.math.structures.cartesian;

import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.ListRepresentation;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RingElementVector extends Vector<RingElement> implements Representable {

    public RingElementVector(RingElement... values) {
        super(values);
    }

    public RingElementVector(List<RingElement> values) {
        super(values);
    }

    protected RingElementVector(RingElement[] values, boolean isSafe) {
        super(values, isSafe);
    }

    protected RingElementVector(List<? extends RingElement> values, boolean isSafe) {
        super(values, isSafe);
    }

    public RingElementVector(Vector<? extends RingElement> vector) {
        this(vector.values, true);
    }

    public RingElementVector mul(Vector<? extends RingElement> other) {
        return zip(other, RingElement::mul, RingElementVector::instantiateWithSafeArray);
    }

    public RingElementVector mul(RingElement elem) {
        return map(g -> g.mul(elem), RingElementVector::instantiateWithSafeArray);
    }

    public RingElementVector mul(BigInteger factor) {
        return map(g -> g.mul(factor), RingElementVector::instantiateWithSafeArray);
    }

    public RingElementVector mul(Long factor) {
        return map(g -> g.mul(factor), RingElementVector::instantiateWithSafeArray);
    }

    public RingElementVector add(RingElement elem) {
        return map(g -> g.add(elem), RingElementVector::instantiateWithSafeArray);
    }

    public RingElementVector pow(BigInteger exponent) {
        return map(g -> g.pow(exponent), RingElementVector::instantiateWithSafeArray);
    }

    public RingElementVector pow(long exponent) {
        return map(g -> g.pow(exponent), RingElementVector::instantiateWithSafeArray);
    }

    public RingElementVector pow(Vector<?> exponents) {
        return zip(exponents, (g,x) ->
                        x instanceof Long ? g.pow((Long) x)
                                : g.pow((BigInteger) x),
                RingElementVector::new);
    }

    public RingElement innerProduct(Vector<? extends RingElement> rightHandSide, RingElement zeroElement) {
        return zipReduce(rightHandSide, RingElement::mul, RingElement::add, zeroElement);
    }

    public RingElement innerProduct(Vector<? extends RingElement> rightHandSide) {
        return innerProduct(rightHandSide, null);
    }

    private static RingElementVector instantiateWithSafeArray(List<? extends RingElement> array) {
        return new RingElementVector(array, true);
    }

    public static RingElementVector iterate(RingElement initialValue, Function<RingElement, RingElement> nextValue, int n) {
        return Vector.iterate(initialValue, nextValue, n, RingElementVector::instantiateWithSafeArray);
    }

    public static RingElementVector generate(Function<Integer, ? extends RingElement> generator, int n) {
        return generatePlain(generator, n, RingElementVector::instantiateWithSafeArray);
    }

    public static RingElementVector generate(Supplier<? extends RingElement> generator, int n) {
        return generatePlain(generator, n, RingElementVector::instantiateWithSafeArray);
    }

    public static RingElementVector of(RingElement... vals) {
        return new RingElementVector(vals, false);
    }

    public static RingElementVector fromStream(Stream<? extends RingElement> stream) {
        return fromStreamPlain(stream, RingElementVector::instantiateWithSafeArray);
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(map(Representable::getRepresentation).toList());
    }

    @Override
    public RingElementVector pad(RingElement valueToPadWith, int desiredLength) {
        return new RingElementVector(super.pad(valueToPadWith, desiredLength));
    }

    @Override
    public RingElementVector replace(int index, RingElement substitute) {
        return new RingElementVector(super.replace(index, substitute));
    }

    @Override
    public RingElementVector truncate(int newLength) {
        return new RingElementVector(super.truncate(newLength));
    }

    public RingElementVector concatenate(Vector<? extends RingElement> secondPart) {
        return new RingElementVector(super.concatenate(secondPart));
    }

    public ProductRingElement asElementInProductRing() {
        return new ProductRingElement(values);
    }
}
