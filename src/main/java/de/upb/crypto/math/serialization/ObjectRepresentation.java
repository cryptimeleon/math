package de.upb.crypto.math.serialization;

import de.upb.crypto.math.serialization.converter.JSONConverter;

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
    protected Map<String, Representation> map = new HashMap<>();

    public ObjectRepresentation() {

    }

    public ObjectRepresentation(String key0, Representation value0) {
        this();
        put(key0, value0);
    }

    public ObjectRepresentation(String key0, Representation value0,
                                String key1, Representation value1) {
        this();
        put(key0, value0);
        put(key1, value1);
    }

    public ObjectRepresentation(String key0, Representation value0,
                                String key1, Representation value1,
                                String key2, Representation value2) {
        this();
        put(key0, value0);
        put(key1, value1);
        put(key2, value2);
    }

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
     * Put an attribute.
     */
    public void put(String key, Representation value) {
        if (key == null || key.isEmpty())
            throw new RuntimeException("Cannot use empty or null keys");
        map.put(key, value);
    }

    /**
     * If key is not in this ObjectRepresentation, calls the supplier and inserts into this ObjectRepresentation whatever it returns.
     * @return The Representation stored at key after this operation
     */
    public Representation putIfMissing(String key, Supplier<Representation> generatorOfValueToPut) {
        return map.computeIfAbsent(key, k -> generatorOfValueToPut.get());
    }

    /**
     * Get an attribute.
     */
    public Representation get(String name) {
        return map.get(name);
    }

    /**
     * Get the backing map to this Representation
     */
    public Map<String, Representation> getMap() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Iterator<Entry<String, Representation>> iterator() {
        return map.entrySet().stream().sorted(Comparator.comparing(Entry::getKey)).iterator();
    }

    public Stream<Entry<String, Representation>> stream() {
        return map.entrySet().stream();
    }

    public void forEach(BiConsumer<String, Representation> consumer) {
        for (Entry<String, Representation> e : getMap().entrySet())
            consumer.accept(e.getKey(), e.getValue());
    }

    public void forEachOrderedByKeys(BiConsumer<String, Representation> consumer) {
        stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEachOrdered(e -> consumer.accept(e.getKey(), e.getValue()));
    }

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
