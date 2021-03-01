package org.cryptimeleon.math.structures.cartesian;

import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A general vector class.
 * <p>
 * For specific instantiations of this class for group and ring elements,
 * see {@link GroupElementVector} and {@link RingElementVector}.
 *
 * @param <X> the type of the vector elements
 */
@SuppressWarnings("unchecked")
public class Vector<X> {
    protected List<? extends X> values;

    public Vector(X... values) {
        this(values, false);
    }

    public Vector(List<? extends X> values) {
        this(values, false);
    }

    public Vector(Vector<? extends X> values) {
        this.values = values.values;
    }

    /**
     * Construct a new {@code Vector} using the given values which are assumed to be safe
     * (promised to not be changed from the outside).
     * @param values the values to construct the new {@code Vector} from
     * @param isSafe whether the given value array is "safe" in the sense that it is promised to not be changed
     *               from the outside afterwards
     */
    protected Vector(X[] values, boolean isSafe){
        this.values = isSafe ? Arrays.asList(values) : new ArrayList<>(Arrays.asList(values));
    }

    /**
     * Construct a new {@code Vector} using the given values which are assumed to be safe
     * (promised to not be changed from the outside).
     * @param values the values to construct the new {@code Vector} from
     * @param isSafe whether the given value array is "safe" in the sense that it is promised to not be changed
     *               from the outside afterwards
     */
    protected Vector(List<? extends X> values, boolean isSafe){
        this.values = isSafe ? values : new ArrayList<>(values);
    }

    /**
     * Construct a new {@code Vector} made up of the given values.
     * @param vals the values to use for constructing the new {@code Vector}
     * @param <Y> the type of the elements
     * @return a new {@code Vector} filled with the given elements
     */
    public static <Y> Vector<Y> of(Y... vals) {
        return new Vector<>(vals);
    }

    /**
     * Construct a new {@code GroupElementVector} made up of the given group elements.
     * @param vals the values to use for constructing the new {@code GroupElementVector}
     * @return a new {@code GroupElementVector} filled with the given elements
     */
    public static GroupElementVector of(GroupElement... vals) {
        return GroupElementVector.of(vals);
    }

    /**
     * Construct a new {@code RingElementVector} made up of the given group elements.
     * @param vals the values to use for constructing the new {@code RingElementVector}
     * @return a new {@code RingElementVector} filled with the given elements
     */
    public static RingElementVector of(RingElement... vals) {
        return RingElementVector.of(vals);
    }

    /**
     * Constructs a new {@code Vector<Y>} with {@code n} elements by applying the {@code nextValue} function
     * {@code n } times, storing each result in the resulting vector.
     *
     * @see Vector#iterate(Object, Function, int, Function)
     *
     * @param initialValue the first value of the resulting vector and the basis for the successive values
     * @param nextValue the function producing values for the resulting vector
     * @param n the length of the resulting vector
     * @param <Y> the type of the vector values
     * @return a new vector of lenghth {@code n} produced by iterative application of the {@code nextValue} function
     */
    public static <Y> Vector<Y> iterate(Y initialValue, Function<Y, Y> nextValue, int n) {
        return iterate(initialValue, nextValue, n, Vector::instantiateWithSafeArray);
    }

    /**
     * Constructs a new {@code Vector} {@code V<Y>} with {@code n} elements by applying the {@code nextValue} function
     * {@code n } times, storing each result in the resulting vector.
     *
     * @param initialValue the first value of the resulting vector and the basis for the successive values
     * @param nextValue the function producing values for the resulting vector
     * @param n the length of the resulting vector
     * @param vectorInstantiator the function that instantiates the vector given a list of values
     * @param <Y> the type of the vector values
     * @param <V> the type of the vector itself
     * @return a new vector of lenghth {@code n} produced by iterative application of the {@code nextValue} function
     */
    protected static <Y,V extends Vector<Y>> V iterate(Y initialValue, Function<Y, Y> nextValue, int n,
                                                       Function<List<Y>, V> vectorInstantiator) {
        List<Y> result = new ArrayList<>(n);
        if (n > 0) {
            result.add(initialValue);
            Y currentValue = initialValue;
            for (int i = 1; i < n; i++) {
                currentValue = nextValue.apply(currentValue);
                result.add(currentValue);
            }
        }
        return vectorInstantiator.apply(result);
    }

    /**
     * Generates a new {@code Vector<Y>} of length {@code n} by applying the {@code generator} function to each
     * index.
     *
     * @param generator the function that creates the vector elements
     * @param n the desired number of elements
     * @param <Y> the type of the vector elements
     * @return a new {@code Vector<Y>} of length {@code n}
     */
    public static <Y> Vector<Y> generatePlain(Function<Integer, ? extends Y> generator, int n) {
        return generatePlain(generator, n, Vector::instantiateWithSafeArray);
    }

    /**
     * Generates a new {@code Vector<Y>} of length {@code n} using the {@code generator} supplier.
     *
     * @param generator the function that creates the vector elements
     * @param n the desired number of elements
     * @param <Y> the type of the vector elements
     * @return a new {@code Vector<Y>} of length {@code n}
     */
    public static <Y> Vector<Y> generatePlain(Supplier<? extends Y> generator, int n) {
        return generatePlain(generator, n, Vector::instantiateWithSafeArray);
    }

    /**
     * Generates a new {@code Vector} {@code V<Y>} of length {@code n} by applying the {@code generator} function
     * to each index.
     *
     * @param generator the function that creates the vector elements
     * @param n the desired number of elements
     * @param vectorInstantiator the function that instantiates the vector given a list of values
     * @param <Y> the type of the vector elements
     * @param <V> the type of vector
     * @return a new {@code Vector<Y>} of length {@code n}
     */
    public static <Y, V extends Vector<Y>> V generatePlain(Function<Integer, ? extends Y> generator, int n,
                                                           Function<List<Y>, V> vectorInstantiator) {
        List<Y> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
            result.add(generator.apply(i));
        return vectorInstantiator.apply(result);
    }

    /**
     * Generates a new {@code Vector} {@code V<Y>} of length {@code n} using the {@code generator} supplier.
     *
     * @param generator the function that creates the vector elements
     * @param n the desired number of elements
     * @param vectorInstantiator the function that instantiates the vector given a list of values
     * @param <Y> the type of the vector elements
     * @param <V> the type of vector
     * @return a new {@code Vector<Y>} of length {@code n}
     */
    public static <Y, V extends Vector<Y>> V generatePlain(Supplier<? extends Y> generator, int n, Function<List<Y>, V> vectorInstantiator) {
        List<Y> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
            result.add(generator.get());
        return vectorInstantiator.apply(result);
    }

    protected static <Z, V extends Vector<Z>> V fromStreamPlain (Stream<? extends Z> stream, Function<List<Z>, V> vectorInstantiator) {
        return vectorInstantiator.apply(stream.collect(Collectors.toList()));
    }

    public static <Z> Vector<Z> fromStreamPlain(Stream<Z> stream) {
        return fromStreamPlain(stream, Vector::instantiateWithSafeArray);
    }

    protected <Y,Z, V extends Vector<Z>> V zip(Vector<Y> other, BiFunction<X, Y, Z> combiner, Function<List<Z>, V> vectorInstantiator) {
        if (values.size() != other.values.size())
            throw new IllegalArgumentException("Can only zip two vectors of the same length");

        ArrayList<Z> result = new ArrayList<>(this.values.size());
        for (int i=0;i<values.size();i++)
            result.add(combiner.apply(this.values.get(i), other.values.get(i)));

        return vectorInstantiator.apply(result);
    }

    public <Y,Z> Vector<Z> zip(Vector<Y> other, BiFunction<X, Y, Z> combiner) {
        return zip(other, combiner, Vector::instantiateWithSafeArray);
    }

    public <Y, V extends Vector<Y>> V map(Function<X, Y> map, Function<List<Y>, ? extends V> vectorInstantiator) {
        ArrayList<Y> result = new ArrayList<>(this.values.size());

        for (X value : values)
            result.add(map.apply(value));

        return vectorInstantiator.apply(result);
    }

    public <Y> Vector<Y> map(Function<X, Y> map) {
        return map(map, Vector::instantiateWithSafeArray);
    }

    public <Y, V extends Vector<Y>> V map(BiFunction<Integer, X, Y> map, Function<List<Y>, ? extends V> vectorInstantiator) {
        ArrayList<Y> result = new ArrayList<>(this.values.size());

        for (int i=0; i<this.values.size();i++)
            result.add(map.apply(i, this.values.get(i)));

        return vectorInstantiator.apply(result);
    }

    public <Y> Vector<Y> map(BiFunction<Integer, X, Y> map) {
        return map(map, Vector::instantiateWithSafeArray);
    }

    public void forEach(Consumer<X> consumer) {
        for (X value : values)
            consumer.accept(value);
    }

    public void forEach(BiConsumer<Integer, X> consumer) {
        for (int i=0;i<values.size();i++)
            consumer.accept(i, values.get(i));
    }

    public X reduce(BinaryOperator<X> combiner) {
        if (values.size() == 0)
            throw new RuntimeException("Cannot reduce empty vector without explicit neutral element");

        return reduce(combiner, null);
    }

    public X reduce(BinaryOperator<X> combiner, X neutralElement) {
        if (values.size() == 0)
            return neutralElement;
        if (values.size() == 1)
            return values.get(0);

        X result = values.get(0);
        for (int i=1;i<values.size();i++)
            result = combiner.apply(result, values.get(i));
        return result;
    }

    /**
     * Equivalent to zip(other, zipMap).reduce(combiner, neutralElement)
     */
    public <Y, Z> Z zipReduce(Vector<Y> other, BiFunction<X, Y, Z> zipMap, BinaryOperator<Z> combiner, Z neutralElement) {
        if (values.size() != other.values.size())
            throw new IllegalArgumentException("Can only zip two vectors of the same length");
        if (values.size() == 0)
            return neutralElement;
        if (values.size() == 1)
            return zipMap.apply(values.get(0), other.values.get(0));

        Z result = zipMap.apply(values.get(0), other.values.get(0));
        for (int i=1;i<values.size();i++)
            result = combiner.apply(result, zipMap.apply(values.get(i), other.values.get(i)));
        return result;
    }

    /**
     * Equivalent to zip(other, zipMap).reduce(combiner, neutralElement)
     */
    public <Y, Z> Z zipReduce(Vector<Y> other, BiFunction<X, Y, Z> zipMap, BinaryOperator<Z> combiner) {
        if (values.size() == 0)
            throw new RuntimeException("Cannot zipReduce empty vector without explicit neutral element");
        return zipReduce(other, zipMap, combiner, null);
    }

    public Vector<X> pad(X valueToPadWith, int desiredLength) {
        ArrayList<X> result = new ArrayList<>(desiredLength);
        result.addAll(values);
        while (result.size() < desiredLength)
            result.add(valueToPadWith);

        return instantiateWithSafeArray(result);
    }

    public Vector<X> replace(int index, X substitute) {
        ArrayList<X> result = new ArrayList<>(values);
        result.set(index, substitute);
        return instantiateWithSafeArray(result);
    }

    public Vector<X> truncate(int newLength) {
        ArrayList<X> result = new ArrayList<>(values.subList(0, newLength));
        return instantiateWithSafeArray(result);
    }

    public Vector<X> concatenate(Vector<? extends X> secondPart) {
        ArrayList<X> result = new ArrayList<>(values.size() + secondPart.values.size());
        result.addAll(values);
        result.add((X) secondPart.values);

        return instantiateWithSafeArray(result);
    }

    public List<X> toList() {
        return new ArrayList<>(values);
    }

    public Stream<? extends X> stream() {
        return values.stream();
    }

    public int length() {
        return values.size();
    }

    public X get(int index) {
        return values.get(index);
    }

    private static <Y> Vector<Y> instantiateWithSafeArray(List<Y> array) {
        return new Vector<>(array, true);
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector)) return false;
        Vector<?> vector = (Vector<?>) o;
        return values.equals(vector.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}
