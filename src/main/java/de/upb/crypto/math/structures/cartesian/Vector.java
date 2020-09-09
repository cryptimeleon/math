package de.upb.crypto.math.structures.cartesian;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class Vector<X> {
    protected X[] values;

    public Vector(X... values) {
        this(values, false);
    }

    protected Vector(X[] values, boolean isSafe){
        this.values = isSafe ? values : Arrays.copyOf(values, values.length);
    }

    public static <Y> Vector<Y> of(Y... vals) {
        return new Vector<>(vals);
    }

    public static GroupElementVector of(GroupElement... vals) {
        return GroupElementVector.of(vals);
    }

    public static RingElementVector of(RingElement... vals) {
        return RingElementVector.of(vals);
    }

    public static <Y> Vector<Y> iterate(Y initialValue, Function<Y, Y> nextValue, int n) {
        return iterate(initialValue, nextValue, n, Vector::instantiateWithSafeArray);
    }

    protected static <Y,V extends Vector<Y>> V iterate(Y initialValue, Function<Y, Y> nextValue, int n, Function<Y[], V> vectorInstantiator) {
        Y[] result = (Y[]) new Object[n];
        if (n > 0) {
            result[0] = initialValue;
            for (int i = 1; i < n; i++)
                result[i] = nextValue.apply(result[i - 1]);
        }
        return vectorInstantiator.apply(result);
    }

    public static <Y> Vector<Y> generatePlain(Function<Integer, ? extends Y> generator, int n) {
        return generatePlain(generator, n, Vector::instantiateWithSafeArray);
    }

    public static <Y> Vector<Y> generatePlain(Supplier<? extends Y> generator, int n) {
        return generatePlain(generator, n, Vector::instantiateWithSafeArray);
    }

    public static <Y, V extends Vector<Y>> V generatePlain(Function<Integer, ? extends Y> generator, int n, Function<Y[], V> vectorInstantiator) {
        Y[] result = (Y[]) new Object[n];
        for (int i = 0; i < n; i++)
            result[i] = generator.apply(i);
        return vectorInstantiator.apply(result);
    }

    public static <Y, V extends Vector<Y>> V generatePlain(Supplier<? extends Y> generator, int n, Function<Y[], V> vectorInstantiator) {
        Y[] result = (Y[]) new Object[n];
        for (int i = 0; i < n; i++)
            result[i] = generator.get();
        return vectorInstantiator.apply(result);
    }

    protected static <Z, V extends Vector<Z>> V fromStreamPlain (Stream<? extends Z> stream, Function<Z[], V> vectorInstantiator) {
        return vectorInstantiator.apply((Z[]) stream.toArray());
    }

    public static <Z> Vector<Z> fromStreamPlain(Stream<Z> stream) {
        return fromStreamPlain(stream, Vector::instantiateWithSafeArray);
    }

    protected <Y,Z, V extends Vector<Z>> V zip(Vector<Y> other, BiFunction<X, Y, Z> combiner, Function<Z[], V> vectorInstantiator) {
        if (values.length != other.values.length)
            throw new IllegalArgumentException("Can only zip two vectors of the same length");

        Z[] result = (Z[]) new Object[this.values.length];
        for (int i=0;i<values.length;i++)
            result[i] = combiner.apply(this.values[i], other.values[i]);

        return vectorInstantiator.apply(result);
    }

    public <Y,Z> Vector<Z> zip(Vector<Y> other, BiFunction<X, Y, Z> combiner) {
        return zip(other, combiner, Vector::instantiateWithSafeArray);
    }

    protected <Y, V extends Vector<Y>> V map(Function<X, Y> map, Function<Y[], ? extends V> vectorInstantiator) {
        Y[] result = (Y[]) new Object[this.values.length];

        for (int i=0;i<values.length;i++)
            result[i] = map.apply(values[i]);

        return vectorInstantiator.apply(result);
    }

    public <Y> Vector<Y> map(Function<X, Y> map) {
        return map(map, Vector::instantiateWithSafeArray);
    }

    public void forEach(Consumer<X> consumer) {
        for (X value : values)
            consumer.accept(value);
    }

    public X reduce(BinaryOperator<X> combiner) {
        if (values.length == 0)
            throw new RuntimeException("Cannot reduce empty vector without explicit neutral element");

        return reduce(combiner, null);
    }

    public X reduce(BinaryOperator<X> combiner, X neutralElement) {
        if (values.length == 0)
            return neutralElement;
        if (values.length == 1)
            return values[0];

        X result = values[0];
        for (int i=1;i<values.length;i++)
            result = combiner.apply(result, values[i]);
        return result;
    }

    /**
     * Equivalent to zip(other, zipMap).reduce(combiner, neutralElement)
     */
    public <Y, Z> Z zipReduce(Vector<Y> other, BiFunction<X, Y, Z> zipMap, BinaryOperator<Z> combiner, Z neutralElement) {
        if (values.length != other.values.length)
            throw new IllegalArgumentException("Can only zip two vectors of the same length");
        if (values.length == 0)
            return neutralElement;
        if (values.length == 1)
            return zipMap.apply(values[0], other.values[0]);

        Z result = zipMap.apply(values[0], other.values[0]);
        for (int i=1;i<values.length;i++)
            result = combiner.apply(result, zipMap.apply(values[i], other.values[i]));
        return result;
    }

    /**
     * Equivalent to zip(other, zipMap).reduce(combiner, neutralElement)
     */
    public <Y, Z> Z zipReduce(Vector<Y> other, BiFunction<X, Y, Z> zipMap, BinaryOperator<Z> combiner) {
        if (values.length == 0)
            throw new RuntimeException("Cannot zipReduce empty vector without explicit neutral element");
        return zipReduce(other, zipMap, combiner, null);
    }

    public List<X> toList() {
        return new ArrayList<>(Arrays.asList(values));
    }

    public Stream<X> stream() {
        return toList().stream();
    }

    public int length() {
        return values.length;
    }

    public X get(int index) {
        return values[index];
    }

    private static <Y> Vector<Y> instantiateWithSafeArray(Y[] array) {
        return new Vector<>(array, true);
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector)) return false;
        Vector<?> vector = (Vector<?>) o;
        return Arrays.equals(values, vector.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }
}
