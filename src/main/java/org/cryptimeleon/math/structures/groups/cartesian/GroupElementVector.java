package org.cryptimeleon.math.structures.groups.cartesian;

import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.cartesian.GroupElementExpressionVector;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMap;
import org.cryptimeleon.math.structures.rings.RingElement;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A vector of group elements supporting various operations on its elements.
 */
public class GroupElementVector extends Vector<GroupElement> implements Representable {

    public GroupElementVector(GroupElement... values) {
        super(values);
    }

    public GroupElementVector(List<GroupElement> values) {
        super(values);
    }

    public GroupElementVector(Vector<? extends GroupElement> vector) {
        super(vector);
    }

    protected GroupElementVector(GroupElement[] values, boolean isSafe) {
        super(values, isSafe);
    }

    protected GroupElementVector(List<? extends GroupElement> values, boolean isSafe) {
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

    public GroupElementVector pow (RingElement exponent) {
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
        if (exp instanceof RingElement)
            return g.pow((RingElement) exp);
        if (exp instanceof Long)
            return g.pow((Long) exp);
        throw new IllegalArgumentException("Cannot compute g^"+exp.getClass().getName());
    }

    public GroupElement innerProduct(Vector<? extends GroupElement> rightHandSide, BilinearMap bilinearMap) {
        return zipReduce(rightHandSide, bilinearMap::apply, GroupElement::op, bilinearMap.getGT().getNeutralElement());
    }

    static GroupElementVector instantiateWithSafeArray(List<? extends GroupElement> array) {
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

    @Override
    public GroupElementVector pad(GroupElement valueToPadWith, int desiredLength) {
        return new GroupElementVector(super.pad(valueToPadWith, desiredLength));
    }

    @Override
    public GroupElementVector replace(int index, GroupElement substitute) {
        return new GroupElementVector(super.replace(index, substitute));
    }

    @Override
    public GroupElementVector truncate(int newLength) {
        return new GroupElementVector(super.truncate(newLength));
    }

    @Override
    public GroupElementVector concatenate(Vector<? extends GroupElement> secondPart) {
        return new GroupElementVector(super.concatenate(secondPart));
    }

    @Override
    public GroupElementVector append(GroupElement valueToAppend) {
        return new GroupElementVector(super.append(valueToAppend));
    }

    @Override
    public GroupElementVector prepend(GroupElement valueToPrepend) {
        return new GroupElementVector(super.prepend(valueToPrepend));
    }

    public static GroupElementVector fromStream(Stream<? extends GroupElement> stream) {
        return fromStreamPlain(stream, GroupElementVector::instantiateWithSafeArray);
    }

    public ProductGroupElement asElementInProductGroup() {
        return new ProductGroupElement(values);
    }

    public GroupElementExpressionVector expr() {
        return map(GroupElement::expr, GroupElementExpressionVector::new);
    }

    public GroupElementVector compute() {
        forEach(GroupElement::compute);
        return this;
    }

    public GroupElementVector computeSync() {
        forEach(GroupElement::computeSync);
        return this;
    }

    public GroupElementVector precomputePow() {
        forEach(g -> g.precomputePow());
        return this;
    }

    public GroupElementVector precomputePow(int windowSize) {
        forEach(g -> g.precomputePow(windowSize));
        return this;
    }
}
