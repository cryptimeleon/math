package de.upb.crypto.math.structures.cartesian;

import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.ListRepresentation;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.integers.IntegerElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class GroupElementVector extends Vector<GroupElement> implements Representable {

    public GroupElementVector(GroupElement... values) {
        super(values);
    }

    public GroupElementVector(Vector<? extends GroupElement> vector) {
        this(vector.values, true);
    }

    protected GroupElementVector(GroupElement[] values, boolean isSafe) {
        super(values, isSafe);
    }

    public GroupElementVector op(Vector<? extends GroupElement> other) {
        return zip(other, GroupElement::op, GroupElementVector::instantiateWithSafeArray);
    }

    public GroupElementVector op(GroupElement elem) {
        return map(g -> g.op(elem), GroupElementVector::instantiateWithSafeArray);
    }

    public GroupElementVector pow(BigInteger exponent) {
        return map(g -> g.pow(exponent), GroupElementVector::instantiateWithSafeArray);
    }

    public GroupElementVector pow(long exponent) {
        return map(g -> g.pow(exponent), GroupElementVector::instantiateWithSafeArray);
    }

    public GroupElementVector pow(Zn.ZnElement exponent) {
        return map(g -> g.pow(exponent), GroupElementVector::instantiateWithSafeArray);
    }

    public GroupElementVector pow(Vector<?> exponents) {
        return zip(exponents, GroupElementVector::exponentiateWithObject, GroupElementVector::instantiateWithSafeArray);
    }

    public GroupElement innerProduct(Vector<?> other, GroupElement neutralElement) {
        return zipReduce(other, GroupElementVector::exponentiateWithObject, GroupElement::op, neutralElement);
    }

    public GroupElement innerProduct(Vector<?> other) {
        return zipReduce(other, GroupElementVector::exponentiateWithObject, GroupElement::op);
    }

    protected static GroupElement exponentiateWithObject(GroupElement g, Object exp) {
        if (exp instanceof BigInteger)
            return g.pow((BigInteger) exp);
        if (exp instanceof Zn.ZnElement)
            return g.pow((Zn.ZnElement) exp);
        if (exp instanceof Long)
            return g.pow((Long) exp);
        if (exp instanceof IntegerElement)
            return g.pow(((IntegerElement) exp).getBigInt());
        throw new IllegalArgumentException("Cannot compute g^"+exp.getClass().getName());
    }

    public GroupElement innerProduct(Vector<? extends GroupElement> rightHandSide, BilinearMap bilinearMap) {
        return zipReduce(rightHandSide, bilinearMap::apply, GroupElement::op, bilinearMap.getGT().getNeutralElement());
    }

    private static GroupElementVector instantiateWithSafeArray(GroupElement[] array) {
        return new GroupElementVector(array, true);
    }

    public static GroupElementVector iterate(GroupElement initialValue, Function<GroupElement, GroupElement> nextValue, int n) {
        return Vector.iterate(initialValue, nextValue, n, GroupElementVector::instantiateWithSafeArray);
    }

    public static GroupElementVector generate(Function<Integer, ? extends GroupElement> generator, int n) {
        return generatePlain(generator, n, GroupElementVector::instantiateWithSafeArray);
    }

    public static GroupElementVector generate(Supplier<? extends GroupElement> generator, int n) {
        return generatePlain(generator, n, GroupElementVector::instantiateWithSafeArray);
    }

    public static GroupElementVector of(GroupElement... vals) {
        return new GroupElementVector(vals, false);
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(map(Representable::getRepresentation).toList());
    }

    public static GroupElementVector fromStream(Stream<? extends GroupElement> stream) {
        return fromStreamPlain(stream, GroupElementVector::instantiateWithSafeArray);
    }

    public ProductGroupElement asElementInProductGroup() {
        return new ProductGroupElement(values);
    }
}
