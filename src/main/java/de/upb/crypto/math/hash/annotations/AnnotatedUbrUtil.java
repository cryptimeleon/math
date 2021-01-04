package de.upb.crypto.math.hash.annotations;

import de.upb.crypto.math.hash.impl.ByteArrayAccumulator;
import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.hash.EscapingByteAccumulator;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Structure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class that allows for almost fully automated byte accumulation of member variables annotated with
 * {@code @UniqueByteRepresented}.
 * <p>
 * Specifically, the {@code updateAccumulator()} method only has to be implemented as
 * <pre>
 * return AnnotatedUbrUtil.autoAccumulate(accumulator, this);
 * </pre>
 * This will append the annotated object members (including those of superclasses) to the accumulator.
 * <p>
 * It is guaranteed that for objects {@code i1} and {@code i2}, if {@code !i1.x.equals(i2.x)}
 * (for some annotated object member {@code x}), then the data appended by
 * {@code AnnotatedUbrUtil.autoAccumulate(acc, i1)} is different from {@code AnnotatedUbrUtil.autoAccumulate(acc, i1)}.
 * This depends on correct implementation of the {@link UniqueByteRepresentable} interface of used classes.
 * Furthermore, the contract of {@code UniqueByteRepresentable} applies.
 * <p>
 * The utility can handle arbitrarily nested lists, sets, arrays, etc.
 * For a complete list of types it can handle, refer to {@link #accumulateObject(ByteAccumulator, Object)}.
 */
public class AnnotatedUbrUtil {
    /**
     * The symbol used to represent null values in unique byte representations.
     */
    protected static final byte nullSymbol = 127;

    /**
     * Contains all annotations used to mark fields that should be automatically accumulated by this class.
     */
    protected static final List<Class<? extends Annotation>> annotationClassList
            = Arrays.asList(UniqueByteRepresented.class);

    /**
     * Adds all {@code @UniqueByteRepresented}-annotated fields within the given instance object to the accumulator.
     *
     * @param accumulator {@code ByteAccumulator} to append to
     * @param instance    instance of class with annotated member variables
     * @return the given accumulator (for chaining)
     */
    public static ByteAccumulator autoAccumulate(ByteAccumulator accumulator, Object instance) {
        ArrayList<Object> accumulatableObjects = new ArrayList<>();

        try {
            Class<?> clazz = instance.getClass();
            while (!clazz.equals(Object.class)) {
                for (Field field : Arrays.stream(clazz.getDeclaredFields())
                        .sorted(Comparator.comparing(Field::getName))
                        .collect(Collectors.toList())) {

                    boolean access = field.isAccessible();
                    field.setAccessible(true);

                    Object fieldValue = field.get(instance);

                    if (hasAnnotation(field)) {
                        accumulatableObjects.add(fieldValue);
                    }
                    field.setAccessible(access);
                }
                clazz = clazz.getSuperclass();
            }
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
            throw new RuntimeException(e);
        }

        return accumulateList(accumulator, accumulatableObjects);
    }

    /**
     * Automatically detects type of obj and appends its (standard) unique-byte-representation to accumulator.
     */
    private static ByteAccumulator accumulateObject(ByteAccumulator accumulator, Object obj) {
        if (obj == null) {
            accumulator.append(new byte[]{nullSymbol});
            return accumulator;
        }
        ByteAccumulator nonNullAcc = new EscapingByteAccumulator(accumulator, nullSymbol);

        if (obj instanceof UniqueByteRepresentable) {
            ((UniqueByteRepresentable) obj).updateAccumulator(nonNullAcc);
            return accumulator;
        }

        if (obj instanceof byte[]) {
            nonNullAcc.append((byte[]) obj);
            return accumulator;
        }

        if (obj instanceof Byte) {
            nonNullAcc.append((byte) obj);
            return accumulator;
        }

        if (obj instanceof String) {
            nonNullAcc.append(((String) obj).getBytes(StandardCharsets.UTF_8));
            return accumulator;
        }

        if (obj instanceof Integer) {
            nonNullAcc.append((Integer) obj);
            return accumulator;
        }

        if (obj instanceof BigInteger) {
            nonNullAcc.append(((BigInteger) obj).toByteArray());
            return accumulator;
        }

        if (obj instanceof List) {
            accumulateList(nonNullAcc, (List) obj);
            return accumulator;
        }

        if (obj.getClass().isArray()) {
            accumulateList(nonNullAcc, Arrays.asList((Object[]) obj));
            return accumulator;
        }

        if (obj instanceof Enum) {
            nonNullAcc.append(((Enum) obj).ordinal());
            return accumulator;
        }

        if (obj instanceof Map) {
            accumulateMap(nonNullAcc, (Map) obj);
            return accumulator;
        }

        if (obj instanceof Set) {
            accumulateSet(nonNullAcc, (Set) obj);
            return accumulator;
        }

        throw new IllegalArgumentException("Don't know how to unique-byte-represent an object of type " + obj.getClass().getName());
    }

    /**
     * Handles a list of objects and appends a unique representation of this list to the given accumulator.
     */
    private static ByteAccumulator accumulateList(ByteAccumulator accumulator, List objects) {
        //Special case: many Elements of the same structure in the list. Can optimize
        if (objects.size() > 0 && objects.stream().allMatch(o -> o instanceof Element)) {
            Structure structure = ((Element) objects.get(0)).getStructure();
            if (structure.getUniqueByteLength().isPresent() &&
                    objects.stream().map(e -> ((Element) e).getStructure()).allMatch(s -> s.equals(structure))) {
                //Can simply append all objects as they are constant length.
                for (Object obj : objects)
                    accumulateObject(accumulator, obj);
            }
        }

        //Standard case
        for (Object obj : objects) {
            accumulateObject(new EscapingByteAccumulator(accumulator), obj);
            accumulator.appendSeperator();
        }

        return accumulator;
    }

    private static ByteAccumulator accumulateSet(ByteAccumulator accumulator, Set obj) {
        byte[][] ubrs = new byte[(obj).size()][];
        int i = 0;

        //Gameplan: reduce to accumulateList by ordering the objects by their UBR

        //Collect objects within the set
        for (Object obj2 : obj) {
            ByteAccumulator acc = new ByteArrayAccumulator();
            accumulateObject(acc, obj2);
            ubrs[i++] = acc.extractBytes();
        }

        //Sort
        Arrays.sort(ubrs, (o1, o2) -> { //Comparator doesn't matter too much, just needs to be consistent
            //Sort by length first
            if (o1.length < o2.length)
                return -1;
            if (o1.length > o2.length)
                return 1;
            //Arrays of same length are tie-broken by their first unequal byte
            for (int j = 0; j < o1.length; j++) {
                if (o1[j] < o2[j])
                    return -1;
                if (o1[j] > o2[j])
                    return 1;
            }
            return 0; //if this happens, the arrays are actually equal
        });

        return accumulateList(accumulator, Arrays.asList(ubrs));
    }

    private static ByteAccumulator accumulateMap(ByteAccumulator accumulator, Map obj) {
        byte[][][] ubrs = new byte[obj.size()][][]; //one entry is a pair [k, v], where k and v are byte arrays.
        int i = 0;

        //Collect escaped ubrs of keys and values within the map
        for (Object key : obj.keySet()) {
            ByteAccumulator accKey = new ByteArrayAccumulator();
            ByteAccumulator accVal = new ByteArrayAccumulator();
            accumulateObject(accKey, key);
            accumulateObject(accVal, obj.get(key));
            ubrs[i++] = new byte[][] { accKey.extractBytes(), accVal.extractBytes() };
        }

        //Sort
        Arrays.sort(ubrs, (o1, o2) -> {
            //Sort by key (first field)
            byte[] key1 = o1[0];
            byte[] key2 = o2[0];
            //Sort by length first
            if (key1.length < key2.length)
                return -1;
            if (key1.length > key2.length)
                return 1;
            //Arrays of same length are tie-broken by their first unequal byte
            for (int j = 0; j < o1.length; j++) {
                if (key1[j] < key2[j])
                    return -1;
                if (key1[j] > key2[j])
                    return 1;
            }
            return 0; //if this happens, the arrays are actually equal
        });

        return accumulateList(accumulator, Arrays.asList(ubrs));
    }

    private static boolean hasAnnotation(Field field) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        if (annotations == null || annotations.length == 0) {
            return false;
        }
        return Arrays.stream(annotations)
                .map(Annotation::annotationType)
                .anyMatch(a -> annotationClassList.parallelStream().anyMatch(a::isAssignableFrom));
    }
}
