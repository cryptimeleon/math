package org.cryptimeleon.math.serialization;

import org.cryptimeleon.math.serialization.converter.JSONConverter;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Representation based on a simple {@code String -> Representation} map.
 */
public class ObjectRepresentation extends Representation implements Iterable<Entry<String, Representation>> {
    private static final long serialVersionUID = -7955738062171729317L;
    /**
     * The map underlying this object representation.
     */
    protected final Map<String, Representation> map = new HashMap<>();

    public ObjectRepresentation() {

    }

    /**
     * Constructor for setting a single key-value pair.
     */
    public ObjectRepresentation(String key0, Representation value0) {
        this();
        put(key0, value0);
    }

    /**
     * Constructor for setting two key-value pairs.
     */
    public ObjectRepresentation(String key0, Representation value0,
                                String key1, Representation value1) {
        this();
        put(key0, value0);
        put(key1, value1);
    }

    /**
     * Constructor for setting three key-value pairs.
     */
    public ObjectRepresentation(String key0, Representation value0,
                                String key1, Representation value1,
                                String key2, Representation value2) {
        this();
        put(key0, value0);
        put(key1, value1);
        put(key2, value2);
    }

    /**
     * Constructor for setting four key-value pairs.
     */
    public ObjectRepresentation(String key0, Representation value0,
                                String key1, Representation value1,
                                String key2, Representation value2,
                                String key3, Representation value3) {
        this();
        put(key0, value0);
        put(key1, value1);
        put(key2, value2);
        put(key3, value3);
    }

    /**
     * Stores the given value under the given key.
     */
    public void put(String key, Representation value) {
        if (key == null || key.isEmpty())
            throw new RuntimeException("Cannot use empty or null keys");
        map.put(key, value);
    }

    /**
     * If key is not in this ObjectRepresentation, calls the supplier and inserts into this ObjectRepresentation
     * whatever it returns.
     * @return the {@code Representation} stored at the given key after this operation
     */
    public Representation putIfMissing(String key, Supplier<Representation> generatorOfValueToPut) {
        return map.computeIfAbsent(key, k -> generatorOfValueToPut.get());
    }

    /**
     * Retrieves the value belonging to the given key.
     */
    public Representation get(String name) {
        return map.get(name);
    }

    /**
     * Retrieves an immutable view of the map underlying this representation.
     */
    public Map<String, Representation> getMap() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns an iterator over the underlying map's entry set sorted by key.
     */
    @Override
    public Iterator<Entry<String, Representation>> iterator() {
        return map.entrySet().stream().sorted(Comparator.comparing(Entry::getKey)).iterator();
    }

    /**
     * Returns a stream with the underlying map's entry set as its source.
     */
    public Stream<Entry<String, Representation>> stream() {
        return map.entrySet().stream();
    }

    /**
     * Applies the given consumer function to each key-value pair in the underlying map.
     * @param consumer the consumer function to apply
     */
    public void forEach(BiConsumer<String, Representation> consumer) {
        for (Entry<String, Representation> e : getMap().entrySet())
            consumer.accept(e.getKey(), e.getValue());
    }

    /**
     * Applies the given consumer function to each key-value pair in the underlying map sorted by key.
     * @param consumer the consumer function to apply
     */
    public void forEachOrderedByKeys(BiConsumer<String, Representation> consumer) {
        stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEachOrdered(e -> consumer.accept(e.getKey(), e.getValue()));
    }

    /**
     * Returns the number of key-value pairs stored in this object representation (size of the underlying map)
     */
    public int size() {
        return map.size();
    }

    @Override
    public String toString() {
        return new JSONConverter().serialize(this);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) { //Eclipse generated
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ObjectRepresentation other = (ObjectRepresentation) obj;
        return this.map.equals(other.map);
    }
}
